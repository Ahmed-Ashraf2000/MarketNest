package com.marketnest.ecommerce.dto.error;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationErrorResponse extends BaseErrorResponse {
    private Map<String, String> errors;

    public ValidationErrorResponse(String message, Map<String, String> errors) {
        setMessage(message);
        this.errors = errors;
    }
}