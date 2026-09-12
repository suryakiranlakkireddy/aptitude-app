package com.aptitudeapp.controller;

import com.aptitudeapp.entity.Quiz;
import com.aptitudeapp.entity.Question;
import com.aptitudeapp.entity.Topic;
import com.aptitudeapp.repository.QuestionRepository;
import com.aptitudeapp.repository.QuizRepository;
import com.aptitudeapp.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/quizzes")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuizRepository quizRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    @GetMapping
    public List<Quiz> list() {
        return quizRepository.findAll();
    }

    @PostMapping
    public Quiz create(@RequestBody Quiz quiz, @RequestParam Long topicId) {
        Topic topic = topicRepository.findById(topicId).orElseThrow();
        quiz.setTopic(topic);
        return quizRepository.save(quiz);
    }

    @PutMapping("/{id}")
    public Quiz update(@PathVariable Long id, @RequestBody Quiz updated) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        quiz.setTitle(updated.getTitle());
        quiz.setDescription(updated.getDescription());
        quiz.setDurationMinutes(updated.getDurationMinutes());
        quiz.setMockTest(updated.isMockTest());
        return quizRepository.save(quiz);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        quizRepository.deleteById(id);
    }

    @PostMapping("/{quizId}/questions")
    public Question addQuestion(@PathVariable Long quizId, @RequestBody Question question) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        question.setQuiz(quiz);
        question.getOptions().forEach(o -> o.setQuestion(question));
        return questionRepository.save(question);
    }

    @DeleteMapping("/questions/{questionId}")
    public void deleteQuestion(@PathVariable Long questionId) {
        questionRepository.deleteById(questionId);
    }
}
