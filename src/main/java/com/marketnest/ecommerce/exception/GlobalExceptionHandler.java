package com.marketnest.ecommerce.exception;

import com.marketnest.ecommerce.dto.error.SimpleErrorResponse;
import com.marketnest.ecommerce.dto.error.ValidationErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse("Validation failed", errors));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(VariantNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleVariantNotFoundException(
            VariantNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(WishlistNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleWishlistNotFoundException(
            WishlistNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleReviewNotFoundException(
            ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<SimpleErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<SimpleErrorResponse> handleUnauthorizedAccessException(
            UnauthorizedAccessException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleCategoryNotFoundException(
            CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleOrderNotFoundException(
            OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handlePaymentNotFoundException(
            PaymentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleCartNotFoundException(
            CartNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleProductNotFoundException(
            ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ProductImageNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleProductImageNotFoundException(
            ProductImageNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AddressNotFound.class)
    public ResponseEntity<SimpleErrorResponse> handleAddressNotFoundException(
            AddressNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidVerificationToken.class)
    public ResponseEntity<SimpleErrorResponse> handleInvalidVerificationToken(
            InvalidVerificationToken ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<SimpleErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new SimpleErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<SimpleErrorResponse> handleAccessDeniedException() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new SimpleErrorResponse("You don't have permission to access this resource"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleGenericException(Exception e) {
        return ResponseEntity.internalServerError()
                .body(new SimpleErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<SimpleErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new SimpleErrorResponse("Database constraint violation"));
    }
}