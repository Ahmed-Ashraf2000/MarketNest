package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.category.CategoryRequestDto;
import com.marketnest.ecommerce.dto.category.CategoryResponseDto;
import com.marketnest.ecommerce.dto.category.CategoryStatusUpdateDto;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.mapper.category.CategoryMapper;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<Category> rootCategories = categoryService.getAllRootCategories();
        List<CategoryResponseDto> response = rootCategories.stream()
                .map(categoryMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(categoryMapper.toResponseDto(category));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCategory(
            @Valid @ModelAttribute CategoryRequestDto categoryDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        Category newCategory = categoryService.createCategory(categoryDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryMapper.toResponseDto(newCategory));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Long categoryId,
            @Valid @ModelAttribute CategoryRequestDto categoryDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        Category updatedCategory = categoryService.updateCategory(categoryId, categoryDto);
        return ResponseEntity.ok(categoryMapper.toResponseDto(updatedCategory));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Category deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}/status")
    public ResponseEntity<CategoryResponseDto> updateCategoryStatus(
            @PathVariable Long categoryId,
            @RequestBody CategoryStatusUpdateDto statusDto) {
        Category category = categoryService.updateCategoryStatus(categoryId, statusDto.isActive());
        return ResponseEntity.ok(categoryMapper.toResponseDto(category));
    }
}