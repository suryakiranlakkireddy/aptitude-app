package com.aptitudeapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"}))
@Data
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    private int totalAttempts = 0;
    private int totalCorrect = 0;
    private int totalWrong = 0;
    private double completionPercent = 0.0;
    private int currentStreakDays = 0;

    private LocalDateTime lastActivityAt;
}
