package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.SubmitQuizRequest;
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
    @PostMapping("/quizzes/{id}/start")
    public ApiResponse<Map<String, Object>> start(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("attempt_id", 1, "quiz_id", id, "status", 1));
    }

    @PostMapping("/quizzes/{id}/submit")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long id, @Valid @RequestBody SubmitQuizRequest request) {
        return ApiResponse.ok(Map.of(
            "attempt_id", 1,
            "score", 0,
            "total_score", 0,
            "correct_count", 0,
            "total_count", request.getAnswers().size(),
            "rank", 1
        ));
    }

    @GetMapping("/attempts/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("attempt_id", id, "answers", java.util.List.of()));
    }
}
