package com.marketnest.ecommerce.mapper.Variant;

import com.marketnest.ecommerce.dto.variant.VariantRequestDto;
import com.marketnest.ecommerce.dto.variant.VariantResponseDto;
import com.marketnest.ecommerce.model.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    @Mapping(target = "title", expression = "java(buildTitle(variant))")
    @Mapping(target = "inStock", expression = "java(isInStock(variant))")
    @Mapping(target = "onSale", expression = "java(isOnSale(variant))")
    VariantResponseDto toResponse(ProductVariant variant);

    List<VariantResponseDto> toResponseList(List<ProductVariant> variants);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "option1Name", source = "option1Name", qualifiedByName = "escapeHtml")
    @Mapping(target = "option1Value", source = "option1Value", qualifiedByName = "escapeHtml")
    @Mapping(target = "option2Name", source = "option2Name", qualifiedByName = "escapeHtml")
    @Mapping(target = "option2Value", source = "option2Value", qualifiedByName = "escapeHtml")
    @Mapping(target = "option3Name", source = "option3Name", qualifiedByName = "escapeHtml")
    @Mapping(target = "option3Value", source = "option3Value", qualifiedByName = "escapeHtml")
    @Mapping(target = "isAvailable", defaultValue = "true")
    @Mapping(target = "position", defaultValue = "0")
    @Mapping(target = "stockQuantity", defaultValue = "0")
    ProductVariant toEntity(VariantRequestDto request);

    List<ProductVariant> toEntityList(List<VariantRequestDto> requests);

    @Named("escapeHtml")
    default String escapeHtml(String input) {
        return input != null ? HtmlUtils.htmlEscape(input) : null;
    }

}