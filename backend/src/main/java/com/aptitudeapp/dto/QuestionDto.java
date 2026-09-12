package com.aptitudeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private String questionText;
    private List<QuestionOptionDto> options;
}
