package com.marketnest.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketnest.ecommerce.dto.auth.*;
import com.marketnest.ecommerce.mapper.auth.UserLoginMapper;
import com.marketnest.ecommerce.model.RefreshToken;
import com.marketnest.ecommerce.model.User;
import com.marketnest.ecommerce.repository.UserRepository;
import com.marketnest.ecommerce.service.auth.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserLoginMapper userLoginMapper;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private LoginHistoryService loginHistoryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Ahmed");
        testUser.setLastName("Ashraf");
        testUser.setRole(User.Role.CUSTOMER);
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        Authentication auth =
                new UsernamePasswordAuthenticationToken("test@example.com", "password123",
                        new ArrayList<>());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-token");

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);
        when(refreshTokenService.getRefreshTokenDuration()).thenReturn(604800L);
        when(userLoginMapper.toLoginResponse(any())).thenReturn(new LoginResponseDto());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(header().exists("Set-Cookie"));

        verify(authenticationManager).authenticate(any());
    }

    @Test
    void login_shouldReturnBadRequest_whenCredentialsInvalid() throws Exception {
        UserLoginDto loginDto = new UserLoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any())).thenThrow(
                new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_shouldCreateUser_whenValidData() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword("Password_123");
        registrationDto.setConfirmPassword("Password_123");
        registrationDto.setFirstName("Ahmed");
        registrationDto.setLastName("Ashraf");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(authService.registerUser(any(), any())).thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"));

        verify(authService).registerUser(any(), eq(User.Role.CUSTOMER));
        verify(tokenService).sendVerificationEmail(eq(testUser), anyString());
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailExists() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto();
        registrationDto.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));

        verify(authService, never()).registerUser(any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void changePassword_shouldUpdatePassword_whenValidData() throws Exception {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setCurrentPassword("oldPassword");
        dto.setNewPassword("newPassword_123");
        dto.setConfirmPassword("newPassword_123");

        mockMvc.perform(patch("/api/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(authService).changePassword(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void forgotPassword_shouldSendResetEmail_whenValidEmail() throws Exception {
        ForgotPasswordDto dto = new ForgotPasswordDto();
        dto.setEmail("test@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset link sent to email"));

        verify(authService).forgotPassword(eq("test@example.com"), anyString());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void logout_shouldClearTokens_whenAuthenticated() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(header().exists("Set-Cookie"));

        verify(refreshTokenService).revokeAllUserTokens(testUser);
    }
}