package com.marketnest.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "coupons")
@Data
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "min_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount = BigDecimal.ZERO;

    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "per_user_limit")
    private Integer perUserLimit = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ElementCollection
    @CollectionTable(name = "coupon_applicable_categories",
            joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "category_id")
    private Set<Long> applicableCategories = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "coupon_applicable_products",
            joinColumns = @JoinColumn(name = "coupon_id"))
    @Column(name = "product_id")
    private Set<Long> applicableProducts = new HashSet<>();

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CouponUsage> usages = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
               now.isAfter(startDate) &&
               now.isBefore(endDate) &&
               (usageLimit == null || usageCount < usageLimit);
    }

    public void incrementUsageCount() {
        this.usageCount++;
    }

    public enum DiscountType {
        PERCENTAGE,
        FIXED_AMOUNT
    }
}