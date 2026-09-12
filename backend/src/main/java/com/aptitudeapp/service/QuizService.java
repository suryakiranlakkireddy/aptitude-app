package com.aptitudeapp.service;

import com.aptitudeapp.dto.QuestionDto;
import com.aptitudeapp.dto.QuestionOptionDto;
import com.aptitudeapp.dto.QuizDto;
import com.aptitudeapp.entity.Quiz;
import com.aptitudeapp.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    public List<QuizDto> listByTopic(Long topicId) {
        List<Quiz> quizzes = topicId != null ? quizRepository.findByTopicId(topicId) : quizRepository.findAll();
        return quizzes.stream().map(q -> new QuizDto(
                q.getId(), q.getTitle(), q.getDescription(), q.getTopic().getName(),
                q.getDurationMinutes(), q.isMockTest(), null
        )).toList();
    }

    public QuizDto getQuizForAttempt(Long quizId) {
        Quiz q = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        List<QuestionDto> questionDtos = q.getQuestions().stream().map(question -> new QuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getOptions().stream()
                        .map(o -> new QuestionOptionDto(o.getId(), o.getOptionText()))
                        .toList()
        )).toList();

        return new QuizDto(q.getId(), q.getTitle(), q.getDescription(), q.getTopic().getName(),
                q.getDurationMinutes(), q.isMockTest(), questionDtos);
    }
}
