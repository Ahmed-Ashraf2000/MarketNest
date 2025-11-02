package com.marketnest.ecommerce.service.category;

import com.marketnest.ecommerce.dto.category.CategoryRequestDto;
import com.marketnest.ecommerce.exception.CategoryNotFoundException;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.repository.CategoryRepository;
import com.marketnest.ecommerce.service.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryRequestDto categoryRequestDto;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic products");
        testCategory.setDisplayOrder(1);

        categoryRequestDto = new CategoryRequestDto();
        categoryRequestDto.setName("Electronics");
        categoryRequestDto.setDescription("Electronic products");
        categoryRequestDto.setDisplayOrder(1);
    }

    @Test
    void getAllRootCategories_shouldReturnAllRootCategories() {
        Category rootCategory1 = new Category();
        rootCategory1.setId(1L);
        rootCategory1.setName("Electronics");

        Category rootCategory2 = new Category();
        rootCategory2.setId(2L);
        rootCategory2.setName("Clothing");

        when(categoryRepository.findRootCategories()).thenReturn(
                List.of(rootCategory1, rootCategory2));

        List<Category> result = categoryService.getAllRootCategories();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
                .containsExactly("Electronics", "Clothing");
        verify(categoryRepository).findRootCategories();
    }

    @Test
    void getAllRootCategories_shouldReturnEmptyList_whenNoCategories() {
        when(categoryRepository.findRootCategories()).thenReturn(List.of());

        List<Category> result = categoryService.getAllRootCategories();

        assertThat(result).isEmpty();
        verify(categoryRepository).findRootCategories();
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenExists() {
        when(categoryRepository.findByIdWithChildren(1L)).thenReturn(Optional.of(testCategory));

        Category result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).findByIdWithChildren(1L);
    }

    @Test
    void getCategoryById_shouldThrowException_whenNotFound() {
        when(categoryRepository.findByIdWithChildren(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");
        verify(categoryRepository).findByIdWithChildren(999L);
    }

    @Test
    void createCategory_shouldCreateRootCategory_whenNoParent() {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image".getBytes());
        categoryRequestDto.setImage(imageFile);

        when(cloudinaryService.uploadImage(any(), "folder")).thenReturn(
                "http://cloudinary.com/image.jpg");
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        Category result = categoryService.createCategory(categoryRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(cloudinaryService).uploadImage(imageFile, "folder");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_shouldCreateChildCategory_whenParentIdProvided() {
        Category parentCategory = new Category();
        parentCategory.setId(1L);
        parentCategory.setName("Electronics");

        categoryRequestDto.setParentId(1L);
        categoryRequestDto.setName("Smartphones");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Category result = categoryService.createCategory(categoryRequestDto);

        assertThat(result).isNotNull();
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getParent()).isNotNull();
        assertThat(categoryCaptor.getValue().getParent().getId()).isEqualTo(1L);
    }

    @Test
    void createCategory_shouldThrowException_whenParentNotFound() {
        categoryRequestDto.setParentId(999L);

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(categoryRequestDto))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("999");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createCategory_shouldNotUploadImage_whenNoImageProvided() {
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        Category result = categoryService.createCategory(categoryRequestDto);

        assertThat(result).isNotNull();
        verify(cloudinaryService, never()).uploadImage(any(), "folder");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldUpdateExistingCategory() {
        categoryRequestDto.setName("Updated Electronics");
        categoryRequestDto.setDescription("Updated description");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(1L, categoryRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Electronics");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void updateCategory_shouldThrowException_whenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(999L, categoryRequestDto))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_shouldUpdateImage_whenNewImageProvided() {
        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new image".getBytes());
        categoryRequestDto.setImage(newImage);
        testCategory.setImageUrl("http://cloudinary.com/old.jpg");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(cloudinaryService.uploadImage(any(), "folder")).thenReturn(
                "http://cloudinary.com/new.jpg");
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(1L, categoryRequestDto);

        assertThat(result.getImageUrl()).isEqualTo("http://cloudinary.com/new.jpg");
        verify(cloudinaryService).uploadImage(newImage, "folder");
    }

    @Test
    void updateCategory_shouldUpdateParent_whenParentIdProvided() {
        Category newParent = new Category();
        newParent.setId(2L);
        newParent.setName("New Parent");

        categoryRequestDto.setParentId(2L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newParent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(1L, categoryRequestDto);

        assertThat(result.getParent()).isNotNull();
        assertThat(result.getParent().getId()).isEqualTo(2L);
    }

    @Test
    void updateCategory_shouldRemoveParent_whenParentIdIsNull() {
        Category parentCategory = new Category();
        parentCategory.setId(2L);
        testCategory.setParent(parentCategory);

        categoryRequestDto.setParentId(null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(1L, categoryRequestDto);

        assertThat(result.getParent()).isNull();
    }

    @Test
    void deleteCategory_shouldDeleteCategory_whenExists() {
        when(categoryRepository.findByIdWithChildren(1L)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void deleteCategory_shouldThrowException_whenNotFound() {
        when(categoryRepository.findByIdWithChildren(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void deleteCategory_shouldDeleteImage_whenImageExists() {
        testCategory.setImageUrl("http://cloudinary.com/image.jpg");

        when(categoryRepository.findByIdWithChildren(1L)).thenReturn(Optional.of(testCategory));
        doNothing().when(categoryRepository).delete(testCategory);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void updateCategoryStatus_shouldUpdateStatus() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategoryStatus(1L, false);

        assertThat(result.getIsActive()).isFalse();
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void updateCategoryStatus_shouldThrowException_whenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategoryStatus(999L, true))
                .isInstanceOf(CategoryNotFoundException.class);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createCategory_shouldHandleEmptyImageFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "", "image/jpeg", new byte[0]);
        categoryRequestDto.setImage(emptyFile);

        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        Category result = categoryService.createCategory(categoryRequestDto);

        assertThat(result).isNotNull();
        verify(cloudinaryService, never()).uploadImage(any(), "folder");
    }

    @Test
    void updateCategory_shouldHandleDisplayOrderChange() {
        categoryRequestDto.setDisplayOrder(5);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Category result = categoryService.updateCategory(1L, categoryRequestDto);

        assertThat(result.getDisplayOrder()).isEqualTo(5);
    }
}