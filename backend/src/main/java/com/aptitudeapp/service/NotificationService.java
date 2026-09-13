package com.aptitudeapp.service;

import com.aptitudeapp.entity.DeviceToken;
import com.aptitudeapp.entity.Progress;
import com.aptitudeapp.repository.DeviceTokenRepository;
import com.aptitudeapp.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final DeviceTokenRepository deviceTokenRepository;
    private final ProgressRepository progressRepository;
    private final FcmService fcmService;

    /**
     * Sends to every device registered for a user. Self-heals: a token FCM reports
     * as no longer valid gets deleted so we stop wasting sends on it.
     * Swallows configuration/network errors (logs only) - a notification failure
     * should never break the caller's request or the nightly job.
     */
    public void sendToUser(Long userId, String title, String body) {
        List<DeviceToken> tokens = deviceTokenRepository.findByUserId(userId);
        for (DeviceToken deviceToken : tokens) {
            try {
                FcmService.SendResult result = fcmService.sendToToken(deviceToken.getToken(), title, body);
                if (result == FcmService.SendResult.INVALID_TOKEN) {
                    deviceTokenRepository.delete(deviceToken);
                }
            } catch (IllegalStateException e) {
                log.warn("Skipping push to user {}: {}", userId, e.getMessage());
                return;
            }
        }
    }

    /**
     * Runs daily at 8 PM server time. Anyone with an active streak (currentStreakDays > 0
     * on at least one topic) whose most recent activity across ALL topics wasn't today
     * gets one reminder, so they don't lose it. One push per user, not per topic.
     */
    @Scheduled(cron = "0 0 20 * * *")
    public void sendStreakReminders() {
        List<Progress> streaking = progressRepository.findByCurrentStreakDaysGreaterThan(0);
        LocalDate today = LocalDate.now();

        Map<Long, int[]> streakDaysByUser = new HashMap<>();
        Map<Long, LocalDate> lastActivityByUser = new HashMap<>();

        for (Progress p : streaking) {
            Long userId = p.getUser().getId();
            streakDaysByUser.merge(userId, new int[]{p.getCurrentStreakDays()},
                    (a, b) -> new int[]{Math.max(a[0], b[0])});
            if (p.getLastActivityAt() != null) {
                LocalDate activityDate = p.getLastActivityAt().toLocalDate();
                lastActivityByUser.merge(userId, activityDate,
                        (a, b) -> a.isAfter(b) ? a : b);
            }
        }

        for (Long userId : streakDaysByUser.keySet()) {
            LocalDate lastActivity = lastActivityByUser.get(userId);
            boolean activeToday = lastActivity != null && lastActivity.isEqual(today);
            if (activeToday) continue;

            int streakDays = streakDaysByUser.get(userId)[0];
            sendToUser(userId, "Don't lose your streak! \uD83D\uDD25",
                    "You're on a " + streakDays + "-day streak. Do a quick quiz today before it resets.");
        }
    }
}
