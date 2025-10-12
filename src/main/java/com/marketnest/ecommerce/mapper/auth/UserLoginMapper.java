package com.marketnest.ecommerce.mapper.auth;

import com.marketnest.ecommerce.dto.auth.LoginResponseDto;
import com.marketnest.ecommerce.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.util.HtmlUtils;

@Mapper(componentModel = "spring")
public interface UserLoginMapper {

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "firstName", target = "firstName", qualifiedByName = "escapeHtml")
    @Mapping(source = "lastName", target = "lastName", qualifiedByName = "escapeHtml")
    @Mapping(source = "email", target = "email", qualifiedByName = "escapeHtml")
    LoginResponseDto toLoginResponse(User user);

    @Named("escapeHtml")
    default String escapeHtml(String input) {
        return input != null ? HtmlUtils.htmlEscape(input) : null;
    }
}