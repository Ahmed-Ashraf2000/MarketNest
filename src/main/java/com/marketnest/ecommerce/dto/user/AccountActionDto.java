package com.marketnest.ecommerce.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for account management actions (deactivate or delete)")
public class AccountActionDto {

    @Schema(
            description = "Type of action to perform on the account",
            example = "DEACTIVATE",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"DEACTIVATE", "DELETE"}
    )
    private ActionType actionType;

    @NotBlank(message = "Password confirmation is required")
    @Schema(
            description = "User's password to confirm the account action. Required for security verification",
            example = "MyPassword123!",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    private String passwordConfirmation;

    @Schema(description = "Enum representing the type of account action to perform")
    public enum ActionType {
        @Schema(description = "Temporarily deactivate the account. Can be reactivated later")
        DEACTIVATE,

        @Schema(description = "Permanently delete the account. This action cannot be undone")
        DELETE
    }
}