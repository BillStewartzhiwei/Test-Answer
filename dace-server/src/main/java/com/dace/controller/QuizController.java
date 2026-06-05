package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.CreateQuestionRequest;
import com.dace.dto.CreateQuizRequest;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class QuizController {
    @PostMapping("/spaces/{id}/quizzes")
    public ApiResponse<Map<String, Object>> create(@PathVariable Long id, @Valid @RequestBody CreateQuizRequest request) {
        return ApiResponse.ok(Map.of("id", 1, "space_id", id, "title", request.getTitle(), "status", 1));
    }

    @GetMapping("/spaces/{id}/quizzes")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable Long id) {
        return ApiResponse.ok(List.of());
    }

    @GetMapping("/quizzes/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("id", id, "title", "Demo Quiz", "questions", List.of()));
    }

    @PutMapping("/quizzes/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody CreateQuizRequest request) {
        return ApiResponse.ok(Map.of("id", id, "title", request.getTitle()));
    }

    @PutMapping("/quizzes/{id}/publish")
    public ApiResponse<Map<String, Object>> publish(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("id", id, "status", 2));
    }

    @PutMapping("/quizzes/{id}/close")
    public ApiResponse<Map<String, Object>> close(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("id", id, "status", 3));
    }

    @PostMapping("/quizzes/{id}/questions")
    public ApiResponse<Map<String, Object>> addQuestion(@PathVariable Long id, @Valid @RequestBody CreateQuestionRequest request) {
        return ApiResponse.ok(Map.of("quiz_id", id, "type", request.getType(), "content", request.getContent()));
    }

    @PutMapping("/questions/{id}")
    public ApiResponse<Map<String, Object>> updateQuestion(@PathVariable Long id, @RequestBody CreateQuestionRequest request) {
        return ApiResponse.ok(Map.of("id", id, "type", request.getType()));
    }

    @DeleteMapping("/questions/{id}")
    public ApiResponse<Map<String, Object>> deleteQuestion(@PathVariable Long id) {
        return ApiResponse.ok(Map.of("id", id, "deleted", true));
    }
}
