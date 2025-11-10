package com.marketnest.ecommerce.dto.user.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for address details in responses")
public class AddressResponseDTO {

    @Schema(description = "Unique identifier of the address", example = "1")
    private Long id;

    @Schema(description = "ID of the user who owns this address", example = "1")
    private Long userId;

    @Schema(description = "Primary address line (street number and name)",
            example = "123 Main Street")
    private String addressLine1;

    @Schema(description = "Secondary address line (apartment, suite, etc.)", example = "Apt 4B",
            nullable = true)
    private String addressLine2;

    @Schema(description = "City name", example = "Cairo")
    private String city;

    @Schema(description = "State or province name", example = "Cairo Governorate", nullable = true)
    private String stateProvince;

    @Schema(description = "Postal or ZIP code", example = "11511")
    private String postalCode;

    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "EG")
    private String countryCode;

    @Schema(description = "Flag indicating if this is the user's default address", example = "true")
    private boolean defaultAddress;
}