package com.aptitudeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class QuizDto {
    private Long id;
    private String title;
    private String description;
    private String topicName;
    private Integer durationMinutes;
    private boolean mockTest;
    private List<QuestionDto> questions; // null for list view, populated for detail view
}
