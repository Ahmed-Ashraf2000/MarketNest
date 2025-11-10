package com.marketnest.ecommerce.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Data Transfer Object for user login history records. Contains information about login attempts and sessions")
public class LoginHistoryDto {
    @Schema(description = "Timestamp when the login occurred", example = "2025-01-15T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "IP address from which the login was initiated", example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Geographic location derived from the IP address",
            example = "Cairo, Egypt")
    private String location;

    @Schema(description = "Information about the device used for login",
            example = "Chrome 120.0, Windows 10")
    private String deviceInfo;

    @Schema(description = "Status of the login attempt", example = "SUCCESS")
    private String status;

    @Schema(description = "Flag indicating whether this login attempt was flagged as suspicious",
            example = "false")
    private boolean suspicious;
}