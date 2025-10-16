package com.marketnest.ecommerce.exception;

public class ProductImageNotFoundException extends RuntimeException {
    public ProductImageNotFoundException(String message) {
        super(message);
    }
}
