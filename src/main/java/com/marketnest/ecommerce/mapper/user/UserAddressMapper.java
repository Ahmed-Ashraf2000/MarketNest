package com.marketnest.ecommerce.mapper.user;

import com.marketnest.ecommerce.dto.user.address.AddressRequestDto;
import com.marketnest.ecommerce.dto.user.address.AddressResponseDTO;
import com.marketnest.ecommerce.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.util.HtmlUtils;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {

    @Mapping(source = "user.userId", target = "userId")
    @Mapping(source = "addressLine1", target = "addressLine1", qualifiedByName = "escapeHtml")
    @Mapping(source = "addressLine2", target = "addressLine2", qualifiedByName = "escapeHtml")
    @Mapping(source = "city", target = "city", qualifiedByName = "escapeHtml")
    @Mapping(source = "stateProvince", target = "stateProvince", qualifiedByName = "escapeHtml")
    @Mapping(source = "postalCode", target = "postalCode", qualifiedByName = "escapeHtml")
    @Mapping(source = "countryCode", target = "countryCode", qualifiedByName = "escapeHtml")
    AddressResponseDTO toResponseDTO(Address address);

    Address toEntity(AddressRequestDto dto);

    @Named("escapeHtml")
    default String escapeHtml(String input) {
        return input != null ? HtmlUtils.htmlEscape(input) : null;
    }
}
