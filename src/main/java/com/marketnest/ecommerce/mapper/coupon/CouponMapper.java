package com.marketnest.ecommerce.mapper.coupon;

import com.marketnest.ecommerce.dto.coupon.CouponResponse;
import com.marketnest.ecommerce.dto.coupon.CreateCouponRequest;
import com.marketnest.ecommerce.dto.coupon.UpdateCouponRequest;
import com.marketnest.ecommerce.model.Coupon;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CouponMapper {

    CouponResponse toResponse(Coupon coupon);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usageCount", constant = "0")
    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", defaultValue = "true")
    @Mapping(target = "perUserLimit", defaultValue = "1")
    @Mapping(target = "minPurchaseAmount", defaultValue = "0")
    Coupon toEntity(CreateCouponRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    @Mapping(target = "usages", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateCouponRequest request, @MappingTarget Coupon coupon);
}