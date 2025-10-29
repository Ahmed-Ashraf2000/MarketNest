package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // UserRepository
    Long countByRole(User.Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER' AND u.createdAt >= :since")
    Long countNewCustomersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER' AND u.active = true")
    Long countActiveCustomers();
}
