package com.marketnest.ecommerce.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
}