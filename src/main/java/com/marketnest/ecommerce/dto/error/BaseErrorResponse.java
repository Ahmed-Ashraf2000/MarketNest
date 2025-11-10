package com.marketnest.ecommerce.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Schema(description = "Base error response containing common error information")
public abstract class BaseErrorResponse {

    @Schema(
            description = "Indicates whether the request was successful. Always false for error responses",
            example = "false"
    )
    private boolean success = false;

    @Schema(
            description = "Human-readable error message describing what went wrong",
            example = "An error occurred while processing your request"
    )
    private String message;

    @Schema(
            description = "Timestamp when the error occurred in ISO format",
            example = "2025-01-15 10:30:45",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}