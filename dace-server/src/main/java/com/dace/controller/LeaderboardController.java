package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.service.LeaderboardService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class LeaderboardController {
    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/quizzes/{id}/leaderboard")
    public ApiResponse<Map<String, Object>> quizLeaderboard(@PathVariable Long id) {
        return ApiResponse.ok(leaderboardService.quizLeaderboard(id));
    }

    @GetMapping("/spaces/{id}/leaderboard")
    public ApiResponse<Map<String, Object>> spaceLeaderboard(@PathVariable Long id) {
        return ApiResponse.ok(leaderboardService.spaceLeaderboard(id));
    }
}
