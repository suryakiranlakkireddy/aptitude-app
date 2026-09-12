package com.aptitudeapp.controller;

import com.aptitudeapp.dto.ProgressDto;
import com.aptitudeapp.security.CurrentUser;
import com.aptitudeapp.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// GET /api/progress/me only ever returns the authenticated user's own data - never another user's.
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/me")
    public List<ProgressDto> myProgress() {
        return progressService.getMyProgress(CurrentUser.id());
    }
}
