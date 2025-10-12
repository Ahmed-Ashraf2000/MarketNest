package com.marketnest.ecommerce.dto.auth;

import lombok.Data;

import java.time.Instant;

@Data
public class LoginHistoryDto {
    private Instant timestamp;
    private String ipAddress;
    private String location;
    private String deviceInfo;
    private String status;
    private boolean suspicious;
}