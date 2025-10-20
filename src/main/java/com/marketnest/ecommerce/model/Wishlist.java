package com.marketnest.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wishlist")
@Data

public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<WishlistItem> wishlistItems = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addWishlistItem(WishlistItem item) {
        wishlistItems.add(item);
        item.setWishlist(this);
    }

    public void removeWishlistItem(WishlistItem item) {
        wishlistItems.remove(item);
        item.setWishlist(null);
    }

    public int getTotalItems() {
        return wishlistItems.size();
    }

    public boolean containsProduct(Long productId) {
        return wishlistItems.stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }
}