package com.marketnest.ecommerce.service.product;

import com.marketnest.ecommerce.dto.image.ImageRequestDto;
import com.marketnest.ecommerce.dto.image.ImageResponseDto;
import com.marketnest.ecommerce.exception.ProductImageNotFoundException;
import com.marketnest.ecommerce.exception.ProductNotFoundException;
import com.marketnest.ecommerce.mapper.image.ImageMapper;
import com.marketnest.ecommerce.model.ProductImage;
import com.marketnest.ecommerce.repository.ProductImageRepository;
import com.marketnest.ecommerce.repository.ProductRepository;
import com.marketnest.ecommerce.service.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ImageMapper imageMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public ImageResponseDto uploadProductImage(Long productId, ImageRequestDto request) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        MultipartFile file = request.getFile();
        String imageUrl = cloudinaryService.uploadProductImage(file);

        ProductImage image = imageMapper.toEntity(request);
        image.setProductId(productId);
        image.setUrl(imageUrl);
        image.setFileName(file.getOriginalFilename());
        image.setFileSize(file.getSize());
        image.setMimeType(file.getContentType());

        ProductImage savedImage = productImageRepository.save(image);
        return imageMapper.toResponse(savedImage);
    }

    @Transactional
    public List<ImageResponseDto> uploadMultipleProductImages(Long productId,
                                                              List<ImageRequestDto> requests) {
        return requests.stream()
                .map(request -> uploadProductImage(productId, request))
                .toList();
    }

    @Transactional
    public void deleteProductImage(Long productId, Long imageId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ProductImageNotFoundException(
                        "Product image not found with ID: " + imageId));

        if (!image.getProductId().equals(productId)) {
            throw new ProductImageNotFoundException(
                    "Image does not belong to product with ID: " + productId);
        }

        productImageRepository.delete(image);
    }

    @Transactional(readOnly = true)
    public List<ImageResponseDto> getProductImages(Long productId, boolean activeOnly) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Product not found with ID: " + productId));

        List<ProductImage> images;
        if (activeOnly) {
            images = productImageRepository.findByProductIdAndIsActiveTrue(productId);
        } else {
            images = productImageRepository.findByProductId(productId);
        }

        return imageMapper.toResponseList(images);
    }
}