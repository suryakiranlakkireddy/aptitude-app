package com.aptitudeapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
@Data
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    private String title;

    @Column(nullable = false)
    private String fileUrl; // Supabase Storage URL (PDF)

    private String thumbnailUrl;

    private LocalDateTime uploadedAt = LocalDateTime.now();
}
