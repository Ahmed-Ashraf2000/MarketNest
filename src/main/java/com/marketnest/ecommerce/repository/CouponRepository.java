package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeAndIsActiveTrue(String code);

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.startDate <= :now AND c.endDate >= :now " +
           "AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit)")
    List<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true " +
           "AND c.startDate <= :now AND c.endDate >= :now " +
           "AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit) " +
           "AND (SIZE(c.applicableCategories) = 0 OR :categoryId MEMBER OF c.applicableCategories) " +
           "AND (SIZE(c.applicableProducts) = 0 OR :productId MEMBER OF c.applicableProducts)")
    List<Coupon> findApplicableCoupons(
            @Param("categoryId") Long categoryId,
            @Param("productId") Long productId,
            @Param("now") LocalDateTime now
    );
}