package com.marketnest.ecommerce.mapper.product;

import com.marketnest.ecommerce.dto.product.ProductRequestDto;
import com.marketnest.ecommerce.dto.product.ProductResponseDto;
import com.marketnest.ecommerce.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "inStock", expression = "java(product.isInStock())")
    @Mapping(target = "lowStock", expression = "java(product.isLowStock())")
    @Mapping(target = "onSale", expression = "java(product.isOnSale())")
    ProductResponseDto toResponse(Product product);

    List<ProductResponseDto> toResponseList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "name", source = "name", qualifiedByName = "escapeHtml")
    @Mapping(target = "description", source = "description", qualifiedByName = "escapeHtml")
    @Mapping(target = "brand", source = "brand", qualifiedByName = "escapeHtml")
    @Mapping(target = "metaTitle", source = "metaTitle", qualifiedByName = "escapeHtml")
    @Mapping(target = "metaDescription", source = "metaDescription", qualifiedByName = "escapeHtml")
    @Mapping(target = "metaKeywords", source = "metaKeywords", qualifiedByName = "escapeHtml")
    @Mapping(target = "isActive", defaultValue = "true")
    @Mapping(target = "isFeatured", defaultValue = "false")
    @Mapping(target = "isTaxable", defaultValue = "true")
    @Mapping(target = "lowStockThreshold", defaultValue = "10")
    Product toEntity(ProductRequestDto request);

    @Named("escapeHtml")
    default String escapeHtml(String input) {
        return input != null ? HtmlUtils.htmlEscape(input) : null;
    }

}