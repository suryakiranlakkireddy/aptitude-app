package com.aptitudeapp.controller;

import com.aptitudeapp.entity.User;
import com.aptitudeapp.repository.SubscriptionRepository;
import com.aptitudeapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Admin-only: aggregate visibility across all users. Regular users never hit this controller
// (enforced by SecurityConfig's hasRole("ADMIN") on /api/admin/**).
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/users")
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        long totalUsers = userRepository.count();
        long activeSubs = subscriptionRepository.findByStatus(com.aptitudeapp.entity.SubscriptionStatus.ACTIVE).size();
        return Map.of(
                "totalUsers", totalUsers,
                "activeSubscriptions", activeSubs
        );
    }
}
