package com.dace.quiz.controller;

import com.dace.quiz.common.ApiResponse;
import com.dace.quiz.dto.ApiModels.AttemptResult;
import com.dace.quiz.dto.ApiModels.SubmitAttemptRequest;
import com.dace.quiz.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class AttemptController {
    private final AttemptService attemptService;

    @PostMapping("/quizzes/{quizId}/attempts")
    public ApiResponse<AttemptResult> submit(HttpServletRequest request, @PathVariable Long quizId, @RequestBody SubmitAttemptRequest body) {
        return ApiResponse.ok(attemptService.submit(userId(request), role(request), quizId, body));
    }

    @GetMapping("/attempts/{attemptId}")
    public ApiResponse<AttemptResult> result(HttpServletRequest request, @PathVariable Long attemptId) {
        return ApiResponse.ok(attemptService.result(userId(request), attemptId));
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String role(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }
}
