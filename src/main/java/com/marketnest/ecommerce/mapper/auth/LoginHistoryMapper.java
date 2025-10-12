package com.marketnest.ecommerce.mapper.auth;

import com.marketnest.ecommerce.dto.auth.LoginHistoryDto;
import com.marketnest.ecommerce.model.LoginHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoginHistoryMapper {
    @Mapping(source = "loginTimestamp", target = "timestamp")
    @Mapping(source = "status", target = "status")
    LoginHistoryDto toDto(LoginHistory loginHistory);

    List<LoginHistoryDto> toDtoList(List<LoginHistory> loginHistories);
}