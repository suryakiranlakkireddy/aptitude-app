package com.aptitudeapp.controller;

import com.aptitudeapp.dto.AttemptResultDto;
import com.aptitudeapp.dto.SubmitAttemptRequest;
import com.aptitudeapp.security.CurrentUser;
import com.aptitudeapp.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @PostMapping("/submit")
    public AttemptResultDto submit(@RequestBody SubmitAttemptRequest req) {
        return attemptService.submit(CurrentUser.id(), req);
    }
}
