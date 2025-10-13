package com.marketnest.ecommerce.dto.error;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public abstract class BaseErrorResponse {
    private boolean success = false;
    private String message;

    private String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
}