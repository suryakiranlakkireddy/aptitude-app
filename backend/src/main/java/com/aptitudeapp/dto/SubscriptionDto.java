package com.aptitudeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SubscriptionDto {
    private boolean active;
    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;
    private double amountPaid;
}
