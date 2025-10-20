package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
}
