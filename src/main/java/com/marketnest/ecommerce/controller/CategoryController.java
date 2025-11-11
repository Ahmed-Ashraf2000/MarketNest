package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.category.CategoryRequestDto;
import com.marketnest.ecommerce.dto.category.CategoryResponseDto;
import com.marketnest.ecommerce.dto.category.CategoryStatusUpdateDto;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.product.ProductResponseDto;
import com.marketnest.ecommerce.mapper.category.CategoryMapper;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.service.category.CategoryService;
import com.marketnest.ecommerce.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@Tag(name = "Category Management", description = "APIs for managing product categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final ProductService productService;

    @Operation(summary = "Get all categories", description = "Retrieves all root categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = CategoryResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        List<Category> rootCategories = categoryService.getAllRootCategories();
        List<CategoryResponseDto> response = rootCategories.stream()
                .map(categoryMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get category by ID", description = "Retrieves a category by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(categoryMapper.toResponseDto(category));
    }

    @Operation(summary = "Create a new category", description = "Creates a new category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(
                            schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Update a category", description = "Updates an existing category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
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

    @Operation(summary = "Delete a category", description = "Deletes a category by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Category deleted successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update category status",
            description = "Updates the status of a category (active/inactive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category status updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = CategoryResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PatchMapping("/{categoryId}/status")
    public ResponseEntity<CategoryResponseDto> updateCategoryStatus(
            @PathVariable Long categoryId,
            @RequestBody CategoryStatusUpdateDto statusDto) {
        Category category = categoryService.updateCategoryStatus(categoryId, statusDto.isActive());
        return ResponseEntity.ok(categoryMapper.toResponseDto(category));
    }

    @Operation(summary = "Get products by category",
            description = "Retrieves products under a specific category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{categoryId}/products")
    public ResponseEntity<?> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort) {

        Sort sorting = Sort.by(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<ProductResponseDto>
                products =
                productService.getProductsByCategoryId(categoryId, activeOnly, pageable);
        return ResponseEntity.ok(products);
    }
}