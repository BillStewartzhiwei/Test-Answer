package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.CreateQuestionRequest;
import com.dace.dto.CreateQuizRequest;
import com.dace.service.QuizService;
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
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/spaces/{id}/quizzes")
    public ApiResponse<Map<String, Object>> create(@PathVariable Long id, @Valid @RequestBody CreateQuizRequest request) {
        return ApiResponse.ok(quizService.create(id, request));
    }

    @GetMapping("/spaces/{id}/quizzes")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable Long id) {
        return ApiResponse.ok(quizService.list(id));
    }

    @GetMapping("/quizzes/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(quizService.detail(id));
    }

    @PutMapping("/quizzes/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody CreateQuizRequest request) {
        return ApiResponse.ok(quizService.update(id, request));
    }

    @PutMapping("/quizzes/{id}/publish")
    public ApiResponse<Map<String, Object>> publish(@PathVariable Long id) {
        return ApiResponse.ok(quizService.publish(id));
    }

    @PutMapping("/quizzes/{id}/close")
    public ApiResponse<Map<String, Object>> close(@PathVariable Long id) {
        return ApiResponse.ok(quizService.close(id));
    }

    @PostMapping("/quizzes/{id}/questions")
    public ApiResponse<Map<String, Object>> addQuestion(@PathVariable Long id, @Valid @RequestBody CreateQuestionRequest request) {
        return ApiResponse.ok(quizService.addQuestion(id, request));
    }

    @PutMapping("/questions/{id}")
    public ApiResponse<Map<String, Object>> updateQuestion(@PathVariable Long id, @RequestBody CreateQuestionRequest request) {
        return ApiResponse.ok(quizService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    public ApiResponse<Map<String, Object>> deleteQuestion(@PathVariable Long id) {
        return ApiResponse.ok(quizService.deleteQuestion(id));
    }
}
