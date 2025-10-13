package com.marketnest.ecommerce.dto.user.profile;

import lombok.Data;

@Data
public class ProfileResponseDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String photoUrl;
    private boolean emailVerified;
}
