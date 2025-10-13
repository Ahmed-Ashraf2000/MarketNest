package com.marketnest.ecommerce.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountActionDto {
    private ActionType actionType;

    @NotBlank(message = "Password confirmation is required")
    private String passwordConfirmation;

    public enum ActionType {
        DEACTIVATE,
        DELETE
    }
}