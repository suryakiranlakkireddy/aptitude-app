package com.aptitudeapp.controller;

import com.aptitudeapp.dto.RegisterDeviceTokenRequest;
import com.aptitudeapp.entity.DeviceToken;
import com.aptitudeapp.entity.User;
import com.aptitudeapp.repository.DeviceTokenRepository;
import com.aptitudeapp.repository.UserRepository;
import com.aptitudeapp.security.CurrentUser;
import com.aptitudeapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @PostMapping("/register")
    public void register(@RequestBody RegisterDeviceTokenRequest req) {
        User user = userRepository.findById(CurrentUser.id()).orElseThrow();
        DeviceToken deviceToken = deviceTokenRepository.findByToken(req.getToken())
                .orElseGet(DeviceToken::new);
        deviceToken.setUser(user);
        deviceToken.setToken(req.getToken());
        deviceToken.setPlatform(req.getPlatform());
        deviceTokenRepository.save(deviceToken);
    }

    @DeleteMapping("/token")
    public void unregister(@RequestParam String token) {
        deviceTokenRepository.deleteByToken(token);
    }

    @PostMapping("/test")
    public void test() {
        notificationService.sendToUser(CurrentUser.id(), "Test notification",
                "If you can see this, push notifications are working.");
    }
}
