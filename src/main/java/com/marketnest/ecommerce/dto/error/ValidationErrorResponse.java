package com.marketnest.ecommerce.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Error response for validation failures. Contains field-specific error messages")
public class ValidationErrorResponse extends BaseErrorResponse {

    @Schema(
            description = "Map of field names to their respective validation error messages",
            example = "{\"email\": \"Invalid email format\", \"password\": \"Password must be at least 8 characters\"}",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Map<String, String> errors;

    public ValidationErrorResponse(String message, Map<String, String> errors) {
        setMessage(message);
        this.errors = errors;
    }
}