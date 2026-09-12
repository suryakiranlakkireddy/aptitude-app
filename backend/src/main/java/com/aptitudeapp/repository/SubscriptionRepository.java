package com.aptitudeapp.repository;

import com.aptitudeapp.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByUserIdOrderByExpiresAtDesc(Long userId);
    List<Subscription> findByStatus(com.aptitudeapp.entity.SubscriptionStatus status);
}
