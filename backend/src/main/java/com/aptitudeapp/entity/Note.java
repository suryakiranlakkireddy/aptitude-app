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
    private String fileUrl; // Supabase Storage OBJECT PATH (not a public URL)

    private String thumbnailUrl; // Supabase Storage OBJECT PATH (optional)

    private LocalDateTime uploadedAt = LocalDateTime.now();
}
