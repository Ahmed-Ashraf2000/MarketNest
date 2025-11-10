package com.marketnest.ecommerce.dto.user.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for creating or updating user addresses")
public class AddressRequestDto {

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Schema(
            description = "Primary address line (street number and name)",
            example = "123 Main Street",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 255
    )
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Schema(
            description = "Secondary address line (apartment, suite, unit, building, floor, etc.)",
            example = "Apt 4B",
            nullable = true,
            maxLength = 255
    )
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(
            description = "City name",
            example = "Cairo",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 100
    )
    private String city;

    @Size(max = 100, message = "State/Province must not exceed 100 characters")
    @Schema(
            description = "State or province name",
            example = "Cairo Governorate",
            nullable = true,
            maxLength = 100
    )
    private String stateProvince;

    @NotBlank(message = "Postal code is required")
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9\\s-]+$", message = "Invalid postal code format")
    @Schema(
            description = "Postal or ZIP code. Must contain only letters, numbers, spaces, and hyphens",
            example = "11511",
            requiredMode = Schema.RequiredMode.REQUIRED,
            maxLength = 20,
            pattern = "^[A-Za-z0-9\\s-]+$"
    )
    private String postalCode;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be 2 uppercase letters")
    @Schema(
            description = "ISO 3166-1 alpha-2 country code (2 uppercase letters)",
            example = "EG",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 2,
            maxLength = 2,
            pattern = "^[A-Z]{2}$"
    )
    private String countryCode;
}