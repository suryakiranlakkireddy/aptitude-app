package com.aptitudeapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens", uniqueConstraints = @UniqueConstraint(columnNames = "token"))
@Data
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token; // FCM registration token

    private String platform; // ANDROID | IOS | WEB (informational only)

    private LocalDateTime createdAt = LocalDateTime.now();
}
