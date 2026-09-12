package com.aptitudeapp.service;

import com.aptitudeapp.dto.AttemptResultDto;
import com.aptitudeapp.dto.SubmitAttemptRequest;
import com.aptitudeapp.entity.*;
import com.aptitudeapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final ProgressRepository progressRepository;

    public AttemptResultDto submit(Long userId, SubmitAttemptRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        Quiz quiz = quizRepository.findById(req.getQuizId()).orElseThrow();

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setTotalQuestions(quiz.getQuestions().size());

        int correct = 0;
        for (Question question : quiz.getQuestions()) {
            Long selectedOptionId = req.getAnswers() != null ? req.getAnswers().get(question.getId()) : null;
            QuestionOption selected = null;
            boolean isCorrect = false;
            if (selectedOptionId != null) {
                selected = question.getOptions().stream()
                        .filter(o -> o.getId().equals(selectedOptionId)).findFirst().orElse(null);
                isCorrect = selected != null && selected.isCorrect();
            }
            if (isCorrect) correct++;

            AttemptAnswer answer = new AttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(selected);
            answer.setCorrect(isCorrect);
            attempt.getAnswers().add(answer);
        }

        attempt.setCorrectCount(correct);
        attempt.setScorePercent(attempt.getTotalQuestions() == 0 ? 0 :
                (correct * 100.0) / attempt.getTotalQuestions());
        attempt.setTimeTakenSeconds(req.getTimeTakenSeconds());
        quizAttemptRepository.save(attempt);

        updateProgress(user, quiz.getTopic(), attempt.getTotalQuestions(), correct);

        return new AttemptResultDto(attempt.getId(), attempt.getTotalQuestions(),
                attempt.getCorrectCount(), attempt.getScorePercent(), attempt.getTimeTakenSeconds());
    }

    private void updateProgress(User user, Topic topic, int totalQuestions, int correct) {
        Progress progress = progressRepository.findByUserIdAndTopicId(user.getId(), topic.getId())
                .orElseGet(() -> {
                    Progress p = new Progress();
                    p.setUser(user);
                    p.setTopic(topic);
                    return p;
                });
        progress.setTotalAttempts(progress.getTotalAttempts() + 1);
        progress.setTotalCorrect(progress.getTotalCorrect() + correct);
        progress.setTotalWrong(progress.getTotalWrong() + (totalQuestions - correct));
        int totalAnswered = progress.getTotalCorrect() + progress.getTotalWrong();
        progress.setCompletionPercent(totalAnswered == 0 ? 0 :
                (progress.getTotalCorrect() * 100.0) / totalAnswered);
        progress.setLastActivityAt(LocalDateTime.now());
        progressRepository.save(progress);
    }
}
