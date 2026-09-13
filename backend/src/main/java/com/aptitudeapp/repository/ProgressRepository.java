package com.aptitudeapp.repository;

import com.aptitudeapp.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByUserId(Long userId);
    Optional<Progress> findByUserIdAndTopicId(Long userId, Long topicId);
    List<Progress> findByCurrentStreakDaysGreaterThan(int days);
}
