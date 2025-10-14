package com.marketnest.ecommerce.mapper.category;

import com.marketnest.ecommerce.dto.category.CategoryResponseDto;
import com.marketnest.ecommerce.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.util.HtmlUtils;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "name", target = "name", qualifiedByName = "escapeHtml")
    @Mapping(source = "slug", target = "slug", qualifiedByName = "escapeHtml")
    @Mapping(source = "description", target = "description", qualifiedByName = "escapeHtml")
    @Mapping(source = "imageUrl", target = "imageUrl", qualifiedByName = "escapeHtml")
    @Mapping(source = "parent.name", target = "parentName", qualifiedByName = "escapeHtml")
    CategoryResponseDto toResponseDto(Category category);

    @Named("escapeHtml")
    default String escapeHtml(String input) {
        return input != null ? HtmlUtils.htmlEscape(input) : null;
    }
}
