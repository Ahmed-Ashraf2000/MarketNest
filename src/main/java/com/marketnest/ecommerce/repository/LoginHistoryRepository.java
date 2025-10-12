package com.marketnest.ecommerce.repository;

import com.marketnest.ecommerce.model.LoginHistory;
import com.marketnest.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserOrderByLoginTimestampDesc(User user);

    Optional<LoginHistory> findTopByUserAndStatusOrderByLoginTimestampDesc(
            User user, LoginHistory.LoginStatus status);

    List<LoginHistory> findByUserAndStatusAndLoginTimestampAfter(
            User user, LoginHistory.LoginStatus status, Instant since);
}