package com.marketnest.ecommerce.exception;

public class CouponUsageLimitExceededException extends RuntimeException {
    public CouponUsageLimitExceededException(String message) {
        super(message);
    }
}