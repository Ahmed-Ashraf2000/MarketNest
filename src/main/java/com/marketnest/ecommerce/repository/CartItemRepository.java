package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteByCartIdAndId(Long cartId, Long itemId);

    int countByCartId(Long cartId);
}