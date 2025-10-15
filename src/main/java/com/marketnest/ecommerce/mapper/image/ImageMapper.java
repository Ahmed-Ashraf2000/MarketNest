package com.marketnest.ecommerce.mapper.image;

import com.marketnest.ecommerce.dto.ImageRequestDto;
import com.marketnest.ecommerce.dto.variant.ImageResponseDto;
import com.marketnest.ecommerce.model.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    ImageResponseDto toResponse(ProductImage image);

    List<ImageResponseDto> toResponseList(List<ProductImage> images);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "width", ignore = true)
    @Mapping(target = "height", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "variant", ignore = true)
    @Mapping(target = "altText", source = "altText", qualifiedByName = "escapeHtml")
    @Mapping(target = "title", source = "title", qualifiedByName = "escapeHtml")
    @Mapping(target = "imageType", defaultValue = "GALLERY")
    @Mapping(target = "position", defaultValue = "0")
    @Mapping(target = "isActive", defaultValue = "true")
    ProductImage toEntity(ImageRequestDto request);

    @Named("escapeHtml")
    default String escapeHtml(String input) {
        return input != null ? HtmlUtils.htmlEscape(input) : null;
    }
}