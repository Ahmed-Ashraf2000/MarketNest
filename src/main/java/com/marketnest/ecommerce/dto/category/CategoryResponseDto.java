package com.marketnest.ecommerce.dto.category;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryResponseDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private Integer displayOrder;
    private Long parentId;
    private String parentName;
    private List<CategoryResponseDto> children = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}