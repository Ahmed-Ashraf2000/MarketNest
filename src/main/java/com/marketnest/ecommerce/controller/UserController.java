package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.SimpleErrorResponse;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.user.profile.ProfilePhotoResponseDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileRequestDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileResponseDto;
import com.marketnest.ecommerce.mapper.user.UserProfileMapper;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.cloudinary.CloudinaryService;
import com.marketnest.ecommerce.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> getUserProfile(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileResponseDto profileDTO = userProfileMapper.toProfileDTO(user);

        return ResponseEntity.ok(profileDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @Valid @RequestBody ProfileRequestDto requestDto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        String email = authentication.getName();

        User updatedUser = userService.updateProfile(email, requestDto);

        ProfileResponseDto profileDTO = userProfileMapper.toProfileDTO(updatedUser);

        return ResponseEntity.ok(profileDTO);
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> updateProfilePhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new SimpleErrorResponse("No file was uploaded"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(new SimpleErrorResponse("Only image files are allowed"));
        }

        String email = authentication.getName();

        String photoUrl = cloudinaryService.uploadImage(file);

        User updatedUser = userService.updateProfilePhoto(email, photoUrl);

        ProfilePhotoResponseDto responseDto = new ProfilePhotoResponseDto();
        responseDto.setPhotoUrl(updatedUser.getPhotoUrl());

        return ResponseEntity.ok(responseDto);
    }
}