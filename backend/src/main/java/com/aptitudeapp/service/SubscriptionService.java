package com.aptitudeapp.service;

import com.aptitudeapp.dto.SubscriptionDto;
import com.aptitudeapp.entity.*;
import com.aptitudeapp.repository.PaymentRepository;
import com.aptitudeapp.repository.SubscriptionRepository;
import com.aptitudeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${app.subscription.price-inr}")
    private double priceInr;

    @Value("${app.subscription.validity-months}")
    private int validityMonths;

    public SubscriptionDto getMySubscription(Long userId) {
        var subOpt = subscriptionRepository.findTopByUserIdOrderByExpiresAtDesc(userId);
        if (subOpt.isEmpty()) {
            return new SubscriptionDto(false, null, null, 0);
        }
        Subscription sub = subOpt.get();
        boolean active = sub.getStatus() == SubscriptionStatus.ACTIVE && sub.getExpiresAt().isAfter(LocalDateTime.now());
        return new SubscriptionDto(active, sub.getPurchasedAt(), sub.getExpiresAt(), sub.getAmountPaid());
    }

    // NOTE: Real payment gateway (e.g. Razorpay) integration is a TODO.
    // This creates a subscription record assuming payment has already succeeded client-side / via webhook.
    public SubscriptionDto purchaseOrRenew(Long userId, String transactionId) {
        User user = userRepository.findById(userId).orElseThrow();

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setAmountPaid(priceInr);
        sub.setPurchasedAt(LocalDateTime.now());
        sub.setExpiresAt(LocalDateTime.now().plusMonths(validityMonths));
        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setSubscription(sub);
        payment.setAmount(priceInr);
        payment.setTransactionId(transactionId != null ? transactionId : UUID.randomUUID().toString());
        payment.setPaymentMethod("UPI");
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        return new SubscriptionDto(true, sub.getPurchasedAt(), sub.getExpiresAt(), sub.getAmountPaid());
    }

    // Runs daily to flip expired subscriptions to EXPIRED status.
    @Scheduled(cron = "0 0 1 * * *")
    public void expireOldSubscriptions() {
        var activeSubs = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        for (Subscription sub : activeSubs) {
            if (sub.getExpiresAt().isBefore(LocalDateTime.now())) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(sub);
            }
        }
    }
}
