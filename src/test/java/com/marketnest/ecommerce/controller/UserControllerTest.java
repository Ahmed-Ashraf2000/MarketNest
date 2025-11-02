package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.user.AccountActionDto;
import com.marketnest.ecommerce.dto.user.UserStatusUpdateDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileRequestDto;
import com.marketnest.ecommerce.dto.user.profile.ProfileResponseDto;
import com.marketnest.ecommerce.mapper.user.UserProfileMapper;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.cloudinary.CloudinaryService;
import com.marketnest.ecommerce.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserProfileMapper userProfileMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    private User testUser;
    private ProfileResponseDto profileResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");

        profileResponseDto = new ProfileResponseDto();
        profileResponseDto.setUserId(1L);
        profileResponseDto.setEmail("test@example.com");
        profileResponseDto.setFirstName("Ahmed");
        profileResponseDto.setLastName("Ashraf");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserProfile_shouldReturnProfile_whenAuthenticated() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userProfileMapper.toProfileDTO(testUser)).thenReturn(profileResponseDto);

        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateUserProfile_shouldUpdateAndReturn_whenValidData() throws Exception {
        ProfileRequestDto requestDto = new ProfileRequestDto();
        requestDto.setFirstName("Jane");
        requestDto.setLastName("Smith");

        when(userService.updateProfile(eq("test@example.com"), any())).thenReturn(testUser);
        when(userProfileMapper.toProfileDTO(testUser)).thenReturn(profileResponseDto);

        mockMvc.perform(put("/api/users/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());

        verify(userService).updateProfile(eq("test@example.com"), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfilePhoto_shouldUploadAndReturn_whenValidImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(cloudinaryService.uploadProfileImage(any())).thenReturn(
                "https://example.com/photo.jpg");
        when(userService.updateProfilePhoto(eq("test@example.com"), anyString())).thenReturn(
                testUser);

        mockMvc.perform(multipart("/api/users/profile/photo")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoUrl").value("https://example.com/photo.jpg"));

        verify(cloudinaryService).uploadProfileImage(any());
        verify(userService).updateProfilePhoto(eq("test@example.com"), anyString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfilePhoto_shouldReturnBadRequest_whenNoFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/users/profile/photo")
                        .file(emptyFile)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No file was uploaded"));

        verify(cloudinaryService, never()).uploadProfileImage(any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void processAccountAction_shouldDeactivate_whenValidPassword() throws Exception {
        AccountActionDto dto = new AccountActionDto();
        dto.setActionType(AccountActionDto.ActionType.DEACTIVATE);
        dto.setPasswordConfirmation("password123");

        mockMvc.perform(post("/api/users/account-action")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value(
                        "Your account has been deactivated successfully."));

        verify(userService).processAccountAction(eq("test@example.com"), any());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllUsers_shouldReturnPageOfUsers() throws Exception {
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), PageRequest.of(0, 10), 1);
        when(userService.getAllUsers(any())).thenReturn(userPage);
        when(userProfileMapper.toProfileDTO(any())).thenReturn(profileResponseDto);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(1));

        verify(userService).getAllUsers(any());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getUserById_shouldReturnUser_whenExists() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);
        when(userProfileMapper.toProfileDTO(testUser)).thenReturn(profileResponseDto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateUserStatus_shouldUpdateStatus() throws Exception {
        UserStatusUpdateDto dto = new UserStatusUpdateDto();
        dto.setActive(false);

        when(userService.updateUserStatus(1L, false)).thenReturn(testUser);

        mockMvc.perform(patch("/api/users/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(userService).updateUserStatus(1L, false);
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deleteUser_shouldDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(userService).deleteUserById(1L);
    }
}