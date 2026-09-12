package com.aptitudeapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "question_options")
@Data
public class QuestionOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private String optionText;

    private boolean correct = false;
}
