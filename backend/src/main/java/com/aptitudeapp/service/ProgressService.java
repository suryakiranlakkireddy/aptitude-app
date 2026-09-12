package com.aptitudeapp.service;

import com.aptitudeapp.dto.ProgressDto;
import com.aptitudeapp.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// Every user's progress is strictly scoped to their own userId - never exposed across users.
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ProgressRepository progressRepository;

    public List<ProgressDto> getMyProgress(Long userId) {
        return progressRepository.findByUserId(userId).stream()
                .map(p -> new ProgressDto(
                        p.getTopic().getName(), p.getTotalAttempts(), p.getTotalCorrect(),
                        p.getTotalWrong(), p.getCompletionPercent(), p.getCurrentStreakDays()
                )).toList();
    }
}
