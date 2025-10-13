package com.marketnest.ecommerce.dto.user.address;

import lombok.Data;

@Data
public class AddressResponseDTO {

    private Long id;

    private Long userId;

    private String addressLine1;

    private String addressLine2;

    private String city;

    private String stateProvince;

    private String postalCode;

    private String countryCode;

    private boolean defaultAddress;
}