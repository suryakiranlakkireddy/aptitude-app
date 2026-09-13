package com.aptitudeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteDto {
    private Long id;
    private Long topicId;
    private String title;
    private String fileUrl;
    private String thumbnailUrl;
    private LocalDateTime uploadedAt;
}
