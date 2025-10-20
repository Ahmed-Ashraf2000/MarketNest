package com.marketnest.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants")
@Data
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sku", unique = true, nullable = false)
    private String sku;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "option1_name")
    private String option1Name;

    @Column(name = "option1_value")
    private String option1Value;

    @Column(name = "option2_name")
    private String option2Name;

    @Column(name = "option2_value")
    private String option2Value;

    @Column(name = "option3_name")
    private String option3Name;

    @Column(name = "option3_value")
    private String option3Value;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "position")
    private Integer position = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();

    @Transient
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    @Transient
    public boolean isOnSale() {
        return compareAtPrice != null && compareAtPrice.compareTo(price) > 0;
    }
}