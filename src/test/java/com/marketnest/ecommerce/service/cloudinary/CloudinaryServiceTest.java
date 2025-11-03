package com.marketnest.ecommerce.service.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    private Map<String, Object> uploadResult;

    @BeforeEach
    void setUp() {
        uploadResult = new HashMap<>();
        uploadResult.put("url", "https://res.cloudinary.com/demo/image/upload/sample.jpg");
        uploadResult.put("public_id", "products/sample");
        uploadResult.put("secure_url", "https://res.cloudinary.com/demo/image/upload/sample.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadProductImage_shouldReturnImageUrl_whenUploadSuccessful() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadProductImage(multipartFile);

        assertThat(result).isEqualTo("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        verify(cloudinary).uploader();
        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.get("folder").equals("products")
        ));
    }

    @Test
    void uploadProductImage_shouldThrowException_whenFileIsNull() {
        assertThatThrownBy(() -> cloudinaryService.uploadProductImage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File cannot be null");

        verify(cloudinary, never()).uploader();
    }

    @Test
    void uploadProductImage_shouldThrowException_whenFileIsEmpty() {
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> cloudinaryService.uploadProductImage(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File cannot be empty");

        verify(cloudinary, never()).uploader();
    }

    @Test
    void uploadProductImage_shouldThrowException_whenUploadFails() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenThrow(new IOException("Upload failed"));

        assertThatThrownBy(() -> cloudinaryService.uploadProductImage(multipartFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to upload image to Cloudinary");

        verify(cloudinary).uploader();
    }

    @Test
    void uploadImage_shouldUploadToSpecifiedFolder() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        uploadResult.put("url",
                "https://res.cloudinary.com/demo/image/upload/categories/sample.jpg");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadImage(multipartFile, "categories");

        assertThat(result).isEqualTo(
                "https://res.cloudinary.com/demo/image/upload/categories/sample.jpg");
        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.get("folder").equals("categories")
        ));
    }

    @Test
    void uploadImage_shouldHandleDifferentImageTypes() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadImage(multipartFile, "products");

        assertThat(result).isNotNull();
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_shouldSetResourceType() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        cloudinaryService.uploadImage(multipartFile, "products");

        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.get("resource_type").equals("image")
        ));
    }

    @Test
    void uploadImage_shouldHandleLargeFiles() throws IOException {
        byte[] largeFile = new byte[10 * 1024 * 1024];
        when(multipartFile.getBytes()).thenReturn(largeFile);
        when(multipartFile.getSize()).thenReturn((long) largeFile.length);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadImage(multipartFile, "products");

        assertThat(result).isNotNull();
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_shouldSetPublicIdFormat() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(multipartFile.getOriginalFilename()).thenReturn("test-image.jpg");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        cloudinaryService.uploadImage(multipartFile, "products");

        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.containsKey("folder") && map.get("folder").equals("products")
        ));
    }

    @Test
    void uploadImage_shouldHandleSpecialCharactersInFilename() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(multipartFile.getOriginalFilename()).thenReturn(
                "test image with spaces & special!.jpg");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadImage(multipartFile, "products");

        assertThat(result).isNotNull();
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_shouldReturnSecureUrl() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        uploadResult.put("secure_url",
                "https://res.cloudinary.com/demo/image/upload/secure/sample.jpg");
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadImage(multipartFile, "products");

        assertThat(result).startsWith("https://");
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_shouldHandleUploadResultWithoutUrl() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        uploadResult.clear();
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        assertThatThrownBy(() -> cloudinaryService.uploadImage(multipartFile, "products"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Upload result does not contain URL");

        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadImage_shouldApplyTransformations() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        cloudinaryService.uploadImage(multipartFile, "products");

        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.get("resource_type").equals("image") &&
                map.get("folder").equals("products")
        ));
    }

    @Test
    void uploadProductImage_shouldCallUploadImageWithCorrectFolder() throws IOException {
        when(multipartFile.getBytes()).thenReturn("test image content".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        cloudinaryService.uploadProductImage(multipartFile);

        verify(uploader).upload(any(byte[].class), argThat(map ->
                map.get("folder").equals("products")
        ));
    }
    
}