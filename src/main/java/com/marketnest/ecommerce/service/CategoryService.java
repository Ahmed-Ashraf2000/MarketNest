package com.marketnest.ecommerce.service;

import com.marketnest.ecommerce.dto.category.CategoryRequestDto;
import com.marketnest.ecommerce.exception.CategoryNotFoundException;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.repository.CategoryRepository;
import com.marketnest.ecommerce.service.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CloudinaryService cloudinaryService;
    private final CategoryRepository categoryRepository;

    public List<Category> getAllRootCategories() {
        return categoryRepository.findRootCategories();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findByIdWithChildren(id)
                .orElseThrow(
                        () -> new CategoryNotFoundException("Category not found with id: " + id));
    }

    @Transactional
    public Category createCategory(CategoryRequestDto categoryDto) {
        Category category = new Category();
        updateCategoryFromDto(category, categoryDto);

        String slug = categoryDto.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
        category.setSlug(slug);

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryRequestDto categoryDto) {
        Category category = getCategoryById(id);
        updateCategoryFromDto(category, categoryDto);
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }

    @Transactional
    public Category updateCategoryStatus(Long id, boolean isActive) {
        Category category = getCategoryById(id);
        category.setIsActive(isActive);
        return categoryRepository.save(category);
    }

    private void updateCategoryFromDto(Category category, CategoryRequestDto dto) {
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            String imageUrl = cloudinaryService.uploadCategoryImage(dto.getImage());
            category.setImageUrl(imageUrl);
        }

        if (dto.getDisplayOrder() != null) {
            category.setDisplayOrder(dto.getDisplayOrder());
        }

        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
    }
}