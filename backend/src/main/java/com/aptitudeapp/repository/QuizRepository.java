package com.aptitudeapp.repository;

import com.aptitudeapp.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByTopicId(Long topicId);
    List<Quiz> findByMockTest(boolean mockTest);
}
