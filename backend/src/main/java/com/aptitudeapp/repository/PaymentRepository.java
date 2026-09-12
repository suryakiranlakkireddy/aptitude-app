package com.aptitudeapp.repository;

import com.aptitudeapp.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByPaidAtDesc(Long userId);
}
