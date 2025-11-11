package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.category.CategoryRequestDto;
import com.marketnest.ecommerce.dto.category.CategoryResponseDto;
import com.marketnest.ecommerce.dto.category.CategoryStatusUpdateDto;
import com.marketnest.ecommerce.dto.product.ProductResponseDto;
import com.marketnest.ecommerce.exception.CategoryNotFoundException;
import com.marketnest.ecommerce.mapper.category.CategoryMapper;
import com.marketnest.ecommerce.model.Category;
import com.marketnest.ecommerce.service.category.CategoryService;
import com.marketnest.ecommerce.service.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private ProductService productService;

    private Category testCategory;
    private CategoryResponseDto categoryResponseDto;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setDescription("Electronic products");
        testCategory.setIsActive(true);
        testCategory.setDisplayOrder(1);

        categoryResponseDto = new CategoryResponseDto();
        categoryResponseDto.setId(1L);
        categoryResponseDto.setName("Electronics");
        categoryResponseDto.setDescription("Electronic products");
        categoryResponseDto.setIsActive(true);
        categoryResponseDto.setDisplayOrder(1);
    }

    @Test
    void getAllCategories_shouldReturnAllRootCategories() throws Exception {
        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Clothing");

        CategoryResponseDto responseDto2 = new CategoryResponseDto();
        responseDto2.setId(2L);
        responseDto2.setName("Clothing");

        when(categoryService.getAllRootCategories()).thenReturn(List.of(testCategory, category2));
        when(categoryMapper.toResponseDto(testCategory)).thenReturn(categoryResponseDto);
        when(categoryMapper.toResponseDto(category2)).thenReturn(responseDto2);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].name").value("Clothing"));

        verify(categoryService).getAllRootCategories();
    }

    @Test
    void getAllCategories_shouldReturnEmptyList_whenNoCategories() throws Exception {
        when(categoryService.getAllRootCategories()).thenReturn(List.of());

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getCategoryById_shouldReturnCategory_whenExists() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(testCategory);
        when(categoryMapper.toResponseDto(testCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"))
                .andExpect(jsonPath("$.description").value("Electronic products"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void getCategoryById_shouldReturnNotFound_whenNotExists() throws Exception {
        when(categoryService.getCategoryById(999L))
                .thenThrow(new CategoryNotFoundException("Category not found with id: 999"));

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_shouldCreateCategory_withValidData() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "test.jpg", "image/jpeg", "test image".getBytes());

        when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(
                testCategory);
        when(categoryMapper.toResponseDto(testCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(multipart("/api/categories")
                        .file(imageFile)
                        .param("name", "Electronics")
                        .param("description", "Electronic products")
                        .param("active", "true")
                        .param("displayOrder", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService).createCategory(any(CategoryRequestDto.class));
    }

    @Test
    void createCategory_shouldReturnBadRequest_withInvalidData() throws Exception {
        mockMvc.perform(multipart("/api/categories")
                        .param("name", "")
                        .param("description", "Electronic products")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void updateCategory_shouldUpdateCategory_withValidData() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "test.jpg", "image/jpeg", "test image".getBytes());

        when(categoryService.updateCategory(eq(1L), any(CategoryRequestDto.class))).thenReturn(
                testCategory);
        when(categoryMapper.toResponseDto(testCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(multipart("/api/categories/1")
                        .file(imageFile)
                        .param("name", "Electronics")
                        .param("description", "Updated description")
                        .param("active", "true")
                        .param("displayOrder", "1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));

        verify(categoryService).updateCategory(eq(1L), any(CategoryRequestDto.class));
    }

    @Test
    void updateCategory_shouldReturnBadRequest_withInvalidData() throws Exception {
        mockMvc.perform(multipart("/api/categories/1")
                        .param("name", "")
                        .param("description", "Description")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCategory_shouldDeleteCategory_whenExists() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void deleteCategory_shouldReturnNotFound_whenNotExists() throws Exception {
        doThrow(new CategoryNotFoundException("Category not found with id: 999"))
                .when(categoryService).deleteCategory(999L);

        mockMvc.perform(delete("/api/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategoryStatus_shouldUpdateStatus() throws Exception {
        CategoryStatusUpdateDto statusDto = new CategoryStatusUpdateDto();
        statusDto.setActive(false);

        testCategory.setIsActive(false);
        categoryResponseDto.setIsActive(false);

        when(categoryService.updateCategoryStatus(1L, false)).thenReturn(testCategory);
        when(categoryMapper.toResponseDto(testCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(patch("/api/categories/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.active").value(false));

        verify(categoryService).updateCategoryStatus(1L, false);
    }

    @Test
    void getProductsByCategory_shouldReturnProducts() throws Exception {
        ProductResponseDto productDto = new ProductResponseDto();
        productDto.setId(1L);
        productDto.setName("Product 1");

        Page<ProductResponseDto> productPage = new PageImpl<>(List.of(productDto));

        when(productService.getProductsByCategoryId(eq(1L), anyBoolean(), any()))
                .thenReturn(productPage);

        mockMvc.perform(get("/api/categories/1/products")
                        .param("activeOnly", "true")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productId").value(1));

        verify(productService).getProductsByCategoryId(eq(1L), eq(true), any());
    }

    @Test
    void getProductsByCategory_shouldUseDefaultParameters() throws Exception {
        Page<ProductResponseDto> emptyPage = new PageImpl<>(List.of());

        when(productService.getProductsByCategoryId(eq(1L), anyBoolean(), any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/categories/1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void createCategory_shouldCreateWithoutImage() throws Exception {
        when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(
                testCategory);
        when(categoryMapper.toResponseDto(testCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(multipart("/api/categories")
                        .param("name", "Electronics")
                        .param("description", "Electronic products")
                        .param("active", "true")
                        .param("displayOrder", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_shouldCreateWithParentId() throws Exception {
        when(categoryService.createCategory(any(CategoryRequestDto.class))).thenReturn(
                testCategory);
        when(categoryMapper.toResponseDto(testCategory)).thenReturn(categoryResponseDto);

        mockMvc.perform(multipart("/api/categories")
                        .param("name", "Smartphones")
                        .param("description", "Mobile phones")
                        .param("parentId", "1")
                        .param("active", "true")
                        .param("displayOrder", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    void updateCategoryStatus_shouldHandleInvalidStatus() throws Exception {
        mockMvc.perform(patch("/api/categories/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}