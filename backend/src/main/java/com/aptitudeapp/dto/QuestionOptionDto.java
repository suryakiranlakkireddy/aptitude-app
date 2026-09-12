package com.aptitudeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionOptionDto {
    private Long id;
    private String optionText;
    // 'correct' intentionally omitted for user-facing responses
}
