package com.aptitudeapp.controller;

import com.aptitudeapp.dto.QuizDto;
import com.aptitudeapp.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public List<QuizDto> list(@RequestParam(required = false) Long topicId) {
        return quizService.listByTopic(topicId);
    }

    @GetMapping("/{id}")
    public QuizDto getOne(@PathVariable Long id) {
        return quizService.getQuizForAttempt(id);
    }
}
