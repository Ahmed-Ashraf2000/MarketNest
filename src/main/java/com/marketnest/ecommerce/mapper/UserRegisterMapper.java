package com.marketnest.ecommerce.mapper;

import com.marketnest.ecommerce.dto.UserRegistrationDto;
import com.marketnest.ecommerce.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserRegisterMapper {

    User toEntity(UserRegistrationDto registrationDto);
}
