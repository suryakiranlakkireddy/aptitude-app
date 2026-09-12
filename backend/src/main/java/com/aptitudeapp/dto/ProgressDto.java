package com.aptitudeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProgressDto {
    private String topicName;
    private int totalAttempts;
    private int totalCorrect;
    private int totalWrong;
    private double completionPercent;
    private int currentStreakDays;
}
