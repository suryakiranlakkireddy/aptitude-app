package com.aptitudeapp.controller;

import com.aptitudeapp.dto.SubscriptionDto;
import com.aptitudeapp.security.CurrentUser;
import com.aptitudeapp.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/me")
    public SubscriptionDto mySubscription() {
        return subscriptionService.getMySubscription(CurrentUser.id());
    }

    // TODO: replace with real Razorpay/UPI webhook verification before going live.
    @PostMapping("/purchase")
    public SubscriptionDto purchase(@RequestParam(required = false) String transactionId) {
        return subscriptionService.purchaseOrRenew(CurrentUser.id(), transactionId);
    }
}
