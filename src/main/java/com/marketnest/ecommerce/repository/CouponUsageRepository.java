package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    int countByCoupon_IdAndUser_UserId(Long couponId, Long userId);

    List<CouponUsage> findByUser_UserId(Long userId);

    List<CouponUsage> findByCoupon_Id(Long couponId);

    boolean existsByCoupon_IdAndOrder_Id(Long couponId, Long orderId);
}