package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUser_UserId(Long userId, Pageable pageable);

    Optional<Order> findByUser_UserIdAndId(Long id, Long userId);
}