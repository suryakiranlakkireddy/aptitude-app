package com.aptitudeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttemptResultDto {
    private Long attemptId;
    private int totalQuestions;
    private int correctCount;
    private double scorePercent;
    private int timeTakenSeconds;
}
