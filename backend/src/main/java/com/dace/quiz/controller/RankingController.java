package com.dace.quiz.controller;

import com.dace.quiz.common.ApiResponse;
import com.dace.quiz.dto.ApiModels.RankingItem;
import com.dace.quiz.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RankingController {
    private final RankingService rankingService;

    @GetMapping("/quizzes/{quizId}/ranking")
    public ApiResponse<List<RankingItem>> quizRanking(HttpServletRequest request, @PathVariable Long quizId) {
        return ApiResponse.ok(rankingService.quizRanking(userId(request), role(request), quizId));
    }

    @GetMapping("/spaces/{spaceId}/ranking")
    public ApiResponse<List<RankingItem>> spaceRanking(HttpServletRequest request, @PathVariable Long spaceId) {
        return ApiResponse.ok(rankingService.spaceRanking(userId(request), role(request), spaceId));
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String role(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }
}
