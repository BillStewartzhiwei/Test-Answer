package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.SubmitQuizRequest;
import com.dace.service.AttemptService;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AttemptController {
    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/quizzes/{id}/start")
    public ApiResponse<Map<String, Object>> start(@PathVariable Long id) {
        return ApiResponse.ok(attemptService.start(id));
    }

    @PostMapping("/quizzes/{id}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long id, @Valid @RequestBody SubmitQuizRequest request) {
        return ApiResponse.ok(attemptService.submit(id, request));
    }

    @GetMapping("/attempts/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(attemptService.detail(id));
    }
}
