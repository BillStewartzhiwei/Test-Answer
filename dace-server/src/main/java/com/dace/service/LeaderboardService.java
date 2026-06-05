package com.dace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.config.AuthContext;
import com.dace.config.CurrentUser;
import com.dace.entity.Quiz;
import com.dace.entity.QuizAttempt;
import com.dace.entity.User;
import com.dace.mapper.QuizAttemptMapper;
import com.dace.mapper.UserMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {
    private final QuizAttemptMapper attemptMapper;
    private final UserMapper userMapper;
    private final PermissionService permissionService;
    private final QuizService quizService;
    private final AttemptService attemptService;

    public LeaderboardService(QuizAttemptMapper attemptMapper, UserMapper userMapper, PermissionService permissionService,
                              QuizService quizService, AttemptService attemptService) {
        this.attemptMapper = attemptMapper;
        this.userMapper = userMapper;
        this.permissionService = permissionService;
        this.quizService = quizService;
        this.attemptService = attemptService;
    }

    public Map<String, Object> quizLeaderboard(Long quizId) {
        Quiz quiz = quizService.requireQuiz(quizId);
        permissionService.requireMember(quiz.getSpaceId());
        CurrentUser current = AuthContext.get();
        List<QuizAttempt> attempts = attemptMapper.selectList(new LambdaQueryWrapper<QuizAttempt>()
            .eq(QuizAttempt::getQuizId, quizId)
            .eq(QuizAttempt::getStatus, 2)
            .orderByDesc(QuizAttempt::getScore)
            .orderByAsc(QuizAttempt::getDurationSec));

        QuizAttempt mine = attemptService.findAttempt(quizId, current.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("quiz_title", quiz.getTitle());
        data.put("total_participants", attempts.size());
        data.put("my_rank", mine == null || !Integer.valueOf(2).equals(mine.getStatus()) ? null : attemptService.rankOf(mine));
        data.put("my_score", mine == null ? null : mine.getScore());
        data.put("list", rankedList(attempts));
        return data;
    }

    public Map<String, Object> spaceLeaderboard(Long spaceId) {
        permissionService.requireMember(spaceId);
        CurrentUser current = AuthContext.get();
        List<QuizAttempt> attempts = attemptMapper.selectList(new LambdaQueryWrapper<QuizAttempt>()
            .eq(QuizAttempt::getSpaceId, spaceId)
            .eq(QuizAttempt::getStatus, 2));

        Map<Long, Integer> totals = attempts.stream().collect(Collectors.groupingBy(
            QuizAttempt::getUserId,
            Collectors.summingInt(QuizAttempt::getScore)
        ));
        List<Map.Entry<Long, Integer>> ranked = totals.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .collect(Collectors.toList());

        Integer myScore = totals.get(current.getId());
        Integer myRank = null;
        for (int i = 0; i < ranked.size(); i++) {
            if (ranked.get(i).getKey().equals(current.getId())) {
                myRank = i + 1;
                break;
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("space_id", spaceId);
        data.put("my_rank", myRank);
        data.put("my_score", myScore);
        data.put("list", java.util.stream.IntStream.range(0, ranked.size())
            .mapToObj(index -> userRankMap(ranked.get(index).getKey(), index + 1))
            .collect(Collectors.toList()));
        return data;
    }

    private List<Map<String, Object>> rankedList(List<QuizAttempt> attempts) {
        return java.util.stream.IntStream.range(0, attempts.size())
            .mapToObj(index -> userRankMap(attempts.get(index).getUserId(), index + 1))
            .collect(Collectors.toList());
    }

    private Map<String, Object> userRankMap(Long userId, int rank) {
        User user = userMapper.selectById(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rank", rank);
        data.put("user_id", userId);
        data.put("nickname", user == null ? "" : user.getNickname());
        data.put("avatar", user == null ? null : user.getAvatarUrl());
        data.put("completed", true);
        return data;
    }
}
