package com.marketnest.ecommerce.controller;

import com.marketnest.ecommerce.dto.error.SimpleErrorResponse;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import com.marketnest.ecommerce.dto.user.AccountActionDto;
import com.marketnest.ecommerce.dto.user.UserStatusUpdateDto;
import com.marketnest.ecommerce.dto.user.profile.ProfilePhotoResponseDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileRequestDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileResponseDto;
import com.marketnest.ecommerce.mapper.user.UserProfileMapper;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.cloudinary.CloudinaryService;
import com.marketnest.ecommerce.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @Operation(summary = "Get user profile",
            description = "Retrieves the profile of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProfileResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> getUserProfile(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProfileResponseDto profileDTO = userProfileMapper.toProfileDTO(user);

        return ResponseEntity.ok(profileDTO);
    }

    @Operation(summary = "Update user profile",
            description = "Updates the profile of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProfileResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
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

    @Operation(summary = "Update profile photo",
            description = "Uploads and updates the profile photo of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile photo updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProfilePhotoResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file uploaded",
                    content = @Content(
                            schema = @Schema(implementation = SimpleErrorResponse.class)))
    })
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

        String photoUrl = cloudinaryService.uploadProfileImage(file);

        User updatedUser = userService.updateProfilePhoto(email, photoUrl);

        ProfilePhotoResponseDto responseDto = new ProfilePhotoResponseDto();
        responseDto.setPhotoUrl(updatedUser.getPhotoUrl());

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "Process account action",
            description = "Processes account actions such as deactivation or deletion.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Account action processed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(
                            schema = @Schema(implementation = ValidationErrorResponse.class)))
    })
    @PostMapping("/account-action")
    public ResponseEntity<?> processAccountAction(
            @Valid @RequestBody AccountActionDto accountActionDto,
            Authentication authentication,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ValidationErrorResponse("Validation failed", errors));
        }

        String email = authentication.getName();

        userService.processAccountAction(email, accountActionDto);

        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");

        if (accountActionDto.getActionType() == AccountActionDto.ActionType.DEACTIVATE) {
            response.put("message", "Your account has been deactivated successfully.");
        } else {
            response.put("message", "Your account has been deleted successfully.");
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProfileResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<Page<ProfileResponseDto>> getAllUsers(
            @PageableDefault(sort = "userId") Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        Page<ProfileResponseDto> userDtos = users.map(userProfileMapper::toProfileDTO);
        return ResponseEntity.ok(userDtos);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProfileResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponseDto> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        ProfileResponseDto profileDTO = userProfileMapper.toProfileDTO(user);
        return ResponseEntity.ok(profileDTO);
    }

    @Operation(summary = "Update user status",
            description = "Updates the status (active/inactive) of a user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateDto statusUpdateDto) {
        User user = userService.updateUserStatus(userId, statusUpdateDto.isActive());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User status updated successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user", description = "Deletes a user by their ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUserById(userId);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User deleted successfully");

        return ResponseEntity.ok(response);
    }
}