package com.marketnest.ecommerce.dto.error;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SimpleErrorResponse extends BaseErrorResponse {
    public SimpleErrorResponse(String message) {
        setMessage(message);
    }
}