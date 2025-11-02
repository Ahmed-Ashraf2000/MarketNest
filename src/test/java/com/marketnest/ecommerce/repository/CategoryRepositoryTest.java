package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private CategoryRepository categoryRepository;

    private Category rootCategory1;
    private Category rootCategory2;
    private Category childCategory;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        rootCategory1 = new Category();
        rootCategory1.setName("Electronics");
        rootCategory1.setDescription("Electronic products");
        rootCategory1.setIsActive(true);
        rootCategory1.setDisplayOrder(1);
        rootCategory1 = categoryRepository.save(rootCategory1);

        rootCategory2 = new Category();
        rootCategory2.setName("Clothing");
        rootCategory2.setDescription("Clothing items");
        rootCategory2.setIsActive(true);
        rootCategory2.setDisplayOrder(2);
        rootCategory2 = categoryRepository.save(rootCategory2);

        childCategory = new Category();
        childCategory.setName("Smartphones");
        childCategory.setDescription("Mobile phones");
        childCategory.setIsActive(true);
        childCategory.setDisplayOrder(1);
        childCategory.setParent(rootCategory1);
        childCategory = categoryRepository.save(childCategory);
    }

    @Test
    void findRootCategories_shouldReturnOnlyRootCategories() {
        List<Category> rootCategories = categoryRepository.findRootCategories();

        assertThat(rootCategories).hasSize(2);
        assertThat(rootCategories).extracting(Category::getName)
                .containsExactly("Electronics", "Clothing");
        assertThat(rootCategories).allMatch(category -> category.getParent() == null);
    }

    @Test
    void findRootCategories_shouldReturnEmptyList_whenNoRootCategories() {
        categoryRepository.deleteAll();

        List<Category> rootCategories = categoryRepository.findRootCategories();

        assertThat(rootCategories).isEmpty();
    }

    @Test
    void findRootCategories_shouldOrderByDisplayOrder() {
        Category rootCategory3 = new Category();
        rootCategory3.setName("Books");
        rootCategory3.setDescription("Books and magazines");
        rootCategory3.setIsActive(true);
        rootCategory3.setDisplayOrder(0);
        categoryRepository.save(rootCategory3);

        List<Category> rootCategories = categoryRepository.findRootCategories();

        assertThat(rootCategories).hasSize(3);
        assertThat(rootCategories.get(0).getName()).isEqualTo("Books");
        assertThat(rootCategories.get(0).getDisplayOrder()).isEqualTo(0);
        assertThat(rootCategories.get(1).getDisplayOrder()).isEqualTo(1);
        assertThat(rootCategories.get(2).getDisplayOrder()).isEqualTo(2);
    }

    @Test
    void findRootCategories_shouldNotIncludeChildCategories() {
        Category anotherChild = new Category();
        anotherChild.setName("Laptops");
        anotherChild.setDescription("Laptop computers");
        anotherChild.setIsActive(true);
        anotherChild.setDisplayOrder(2);
        anotherChild.setParent(rootCategory1);
        categoryRepository.save(anotherChild);

        List<Category> rootCategories = categoryRepository.findRootCategories();

        assertThat(rootCategories).hasSize(2);
        assertThat(rootCategories).extracting(Category::getName)
                .doesNotContain("Smartphones", "Laptops");
    }

    @Test
    void findByIdWithChildren_shouldReturnCategoryWithChildren() {
        Optional<Category> result = categoryRepository.findByIdWithChildren(rootCategory1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
        assertThat(result.get().getChildren()).hasSize(1);
        assertThat(result.get().getChildren().getFirst().getName()).isEqualTo("Smartphones");
    }

    @Test
    void findByIdWithChildren_shouldReturnEmpty_whenCategoryNotFound() {
        Optional<Category> result = categoryRepository.findByIdWithChildren(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdWithChildren_shouldReturnCategoryWithEmptyChildren_whenNoChildren() {
        Optional<Category> result = categoryRepository.findByIdWithChildren(rootCategory2.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Clothing");
        assertThat(result.get().getChildren()).isEmpty();
    }

    @Test
    void findByIdWithChildren_shouldLoadChildrenEagerly() {
        Category parent = new Category();
        parent.setName("Parent");
        parent.setDescription("Parent category");
        parent.setIsActive(true);
        parent.setDisplayOrder(1);
        parent = categoryRepository.save(parent);

        Category child1 = new Category();
        child1.setName("Child 1");
        child1.setDescription("First child");
        child1.setIsActive(true);
        child1.setDisplayOrder(1);
        child1.setParent(parent);
        categoryRepository.save(child1);

        Category child2 = new Category();
        child2.setName("Child 2");
        child2.setDescription("Second child");
        child2.setIsActive(true);
        child2.setDisplayOrder(2);
        child2.setParent(parent);
        categoryRepository.save(child2);

        Optional<Category> result = categoryRepository.findByIdWithChildren(parent.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getChildren()).hasSize(2);
        assertThat(result.get().getChildren()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Child 1", "Child 2");
    }

    @Test
    void save_shouldPersistCategory() {
        Category newCategory = new Category();
        newCategory.setName("Sports");
        newCategory.setDescription("Sports equipment");
        newCategory.setIsActive(true);
        newCategory.setDisplayOrder(3);

        Category saved = categoryRepository.save(newCategory);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Sports");
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    void save_shouldPersistCategoryWithParent() {
        Category newChild = new Category();
        newChild.setName("Tablets");
        newChild.setDescription("Tablet devices");
        newChild.setIsActive(true);
        newChild.setDisplayOrder(3);
        newChild.setParent(rootCategory1);

        Category saved = categoryRepository.save(newChild);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getParent()).isNotNull();
        assertThat(saved.getParent().getId()).isEqualTo(rootCategory1.getId());
    }

    @Test
    void findById_shouldReturnCategory_whenExists() {
        Optional<Category> result = categoryRepository.findById(rootCategory1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Category> result = categoryRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void delete_shouldRemoveCategory() {
        categoryRepository.delete(childCategory);
        categoryRepository.flush();

        Optional<Category> result = categoryRepository.findById(childCategory.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllCategories() {
        List<Category> allCategories = categoryRepository.findAll();

        assertThat(allCategories).hasSize(3);
    }

    @Test
    void findRootCategories_shouldHandleInactiveCategories() {
        rootCategory1.setIsActive(false);
        categoryRepository.save(rootCategory1);

        List<Category> rootCategories = categoryRepository.findRootCategories();

        assertThat(rootCategories).hasSize(2);
        assertThat(rootCategories).anyMatch(category -> !category.getIsActive());
    }

    @Test
    void findByIdWithChildren_shouldHandleNestedHierarchy() {
        Category grandChild = new Category();
        grandChild.setName("Android Phones");
        grandChild.setDescription("Android smartphones");
        grandChild.setIsActive(true);
        grandChild.setDisplayOrder(1);
        grandChild.setParent(childCategory);
        categoryRepository.save(grandChild);

        Optional<Category> result = categoryRepository.findByIdWithChildren(rootCategory1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getChildren()).hasSize(1);
    }

    @Test
    void save_shouldUpdateExistingCategory() {
        rootCategory1.setName("Updated Electronics");
        rootCategory1.setDescription("Updated description");

        Category updated = categoryRepository.save(rootCategory1);

        assertThat(updated.getId()).isEqualTo(rootCategory1.getId());
        assertThat(updated.getName()).isEqualTo("Updated Electronics");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
    }

    @Test
    void findRootCategories_shouldHandleDuplicateDisplayOrder() {
        Category rootCategory3 = new Category();
        rootCategory3.setName("Home & Garden");
        rootCategory3.setDescription("Home products");
        rootCategory3.setIsActive(true);
        rootCategory3.setDisplayOrder(1);
        categoryRepository.save(rootCategory3);

        List<Category> rootCategories = categoryRepository.findRootCategories();

        assertThat(rootCategories).hasSize(3);
        assertThat(rootCategories.stream()
                .filter(c -> c.getDisplayOrder() == 1)
                .count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findByIdWithChildren_shouldHandleNullParent() {
        Optional<Category> result = categoryRepository.findByIdWithChildren(childCategory.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getParent()).isNotNull();
    }
}