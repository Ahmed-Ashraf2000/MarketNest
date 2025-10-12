package com.marketnest.ecommerce.dto.auth;

import com.marketnest.ecommerce.model.User.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private Role role;
    private String photoUrl;
    private LocalDateTime lastLoginAt;
}