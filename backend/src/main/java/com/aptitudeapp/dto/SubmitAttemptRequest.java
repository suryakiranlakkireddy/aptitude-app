package com.aptitudeapp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SubmitAttemptRequest {
    private Long quizId;
    // key: questionId, value: selectedOptionId
    private Map<Long, Long> answers;
    private int timeTakenSeconds;
}
