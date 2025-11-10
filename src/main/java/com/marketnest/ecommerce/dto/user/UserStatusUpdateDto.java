package com.marketnest.ecommerce.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for updating user account status (admin operation)")
public class UserStatusUpdateDto {

    @Schema(
            description = "Flag to activate or deactivate the user account",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean active;
}