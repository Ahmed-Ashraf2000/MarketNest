package com.marketnest.ecommerce.dto.variant;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantRequestDto {

    @NotBlank(message = "Variant SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    @Size(max = 100, message = "Barcode must not exceed 100 characters")
    private String barcode;

    @Size(max = 50, message = "Option1 name must not exceed 50 characters")
    private String option1Name;

    @Size(max = 100, message = "Option1 value must not exceed 100 characters")
    private String option1Value;

    @Size(max = 50, message = "Option2 name must not exceed 50 characters")
    private String option2Name;

    @Size(max = 100, message = "Option2 value must not exceed 100 characters")
    private String option2Value;

    @Size(max = 50, message = "Option3 name must not exceed 50 characters")
    private String option3Name;

    @Size(max = 100, message = "Option3 value must not exceed 100 characters")
    private String option3Value;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have max 8 digits and 2 decimals")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Compare at price must be greater than 0")
    @Digits(integer = 8, fraction = 2,
            message = "Compare at price must have max 8 digits and 2 decimals")
    private BigDecimal compareAtPrice;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Weight must have max 8 digits and 2 decimals")
    private BigDecimal weight;

    private Boolean isAvailable;

    @Min(value = 0, message = "Position cannot be negative")
    private Integer position;
}