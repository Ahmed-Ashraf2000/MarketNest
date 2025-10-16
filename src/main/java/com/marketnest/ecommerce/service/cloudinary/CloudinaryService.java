package com.marketnest.ecommerce.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder) {
        try {
            var params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto",
                    "overwrite", true
            );

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }

    public String uploadProfileImage(MultipartFile file) {
        return uploadImage(file, "profiles");
    }

    public String uploadCategoryImage(MultipartFile file) {
        return uploadImage(file, "categories");
    }

    public String uploadProductImage(MultipartFile file) {
        return uploadImage(file, "products");
    }
}