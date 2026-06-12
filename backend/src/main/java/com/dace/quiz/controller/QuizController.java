package com.dace.quiz.controller;

import com.dace.quiz.common.ApiResponse;
import com.dace.quiz.dto.ApiModels.QuizRequest;
import com.dace.quiz.entity.OptionItem;
import com.dace.quiz.entity.Question;
import com.dace.quiz.entity.Quiz;
import com.dace.quiz.service.QuizService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/spaces/{spaceId}/quizzes")
    public ApiResponse<List<Quiz>> list(HttpServletRequest request, @PathVariable Long spaceId) {
        return ApiResponse.ok(quizService.listBySpace(userId(request), role(request), spaceId));
    }

    @PostMapping("/quizzes")
    public ApiResponse<Quiz> create(HttpServletRequest request, @RequestBody QuizRequest body) {
        return ApiResponse.ok(quizService.create(userId(request), role(request), body));
    }

    @PutMapping("/quizzes/{quizId}")
    public ApiResponse<Quiz> update(HttpServletRequest request, @PathVariable Long quizId, @RequestBody QuizRequest body) {
        return ApiResponse.ok(quizService.update(userId(request), quizId, body));
    }

    @GetMapping("/quizzes/{quizId}")
    public ApiResponse<QuizDetail> detail(HttpServletRequest request, @PathVariable Long quizId) {
        Quiz quiz = quizService.detail(userId(request), role(request), quizId);
        QuizDetail detail = new QuizDetail();
        detail.setQuiz(quiz);
        detail.setQuestions(quizService.questions(quizId).stream().map(question -> {
            QuestionView view = new QuestionView();
            view.setQuestion(question);
            List<OptionItem> options = quizService.options(question.getId());
            if ("PARTICIPANT".equals(role(request))) {
                options.forEach(option -> option.setCorrect(null));
            }
            view.setOptions(options);
            return view;
        }).collect(Collectors.toList()));
        return ApiResponse.ok(detail);
    }

    @PostMapping("/quizzes/{quizId}/publish")
    public ApiResponse<Void> publish(HttpServletRequest request, @PathVariable Long quizId) {
        quizService.publish(userId(request), quizId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/quizzes/{quizId}/close")
    public ApiResponse<Void> close(HttpServletRequest request, @PathVariable Long quizId) {
        quizService.close(userId(request), quizId);
        return ApiResponse.ok(null);
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String role(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }

    @Data
    public static class QuizDetail {
        private Quiz quiz;
        private List<QuestionView> questions;
    }

    @Data
    public static class QuestionView {
        private Question question;
        private List<OptionItem> options;
    }
}
