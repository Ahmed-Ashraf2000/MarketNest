package com.marketnest.ecommerce.mapper.user;

import com.marketnest.ecommerce.dto.user.profile.ProfileResponseDto;
import com.marketnest.ecommerce.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    ProfileResponseDto toProfileDTO(User user);
}