package com.dace.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.quiz.dto.ApiModels.RankingItem;
import com.dace.quiz.common.BusinessException;
import com.dace.quiz.entity.Quiz;
import com.dace.quiz.entity.QuizAttempt;
import com.dace.quiz.entity.User;
import com.dace.quiz.mapper.QuizAttemptMapper;
import com.dace.quiz.mapper.QuizMapper;
import com.dace.quiz.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {
    private final QuizMapper quizMapper;
    private final QuizAttemptMapper attemptMapper;
    private final UserMapper userMapper;
    private final GuardService guardService;

    public List<RankingItem> quizRanking(Long userId, String role, Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BusinessException(404, "题目套不存在");
        }
        if ("CREATOR".equals(role)) {
            guardService.requireCreatorSpace(quiz.getSpaceId(), userId);
        } else {
            guardService.requireSpaceMember(quiz.getSpaceId(), userId);
        }
        List<QuizAttempt> attempts = attemptMapper.selectList(new LambdaQueryWrapper<QuizAttempt>().eq(QuizAttempt::getQuizId, quizId));
        attempts.sort(Comparator.comparing(QuizAttempt::getScore).reversed()
                .thenComparing(QuizAttempt::getDurationSeconds)
                .thenComparing(QuizAttempt::getSubmittedAt));
        return toRankingItems(attempts);
    }

    public List<RankingItem> spaceRanking(Long userId, String role, Long spaceId) {
        if ("CREATOR".equals(role)) {
            guardService.requireCreatorSpace(spaceId, userId);
        } else {
            guardService.requireSpaceMember(spaceId, userId);
        }
        List<QuizAttempt> attempts = attemptMapper.selectList(new LambdaQueryWrapper<QuizAttempt>().eq(QuizAttempt::getSpaceId, spaceId));
        Map<Long, RankingItem> grouped = new LinkedHashMap<>();
        for (QuizAttempt attempt : attempts) {
            RankingItem item = grouped.computeIfAbsent(attempt.getUserId(), id -> baseItem(id));
            item.setScore(item.getScore() + attempt.getScore());
            item.setTotalScore(item.getTotalScore() + attempt.getTotalScore());
            item.setDurationSeconds(item.getDurationSeconds() + attempt.getDurationSeconds());
            if (item.getSubmittedAt() == null || attempt.getSubmittedAt().isAfter(item.getSubmittedAt())) {
                item.setSubmittedAt(attempt.getSubmittedAt());
            }
        }
        List<RankingItem> result = grouped.values().stream()
                .sorted(Comparator.comparing(RankingItem::getScore).reversed()
                        .thenComparing(RankingItem::getDurationSeconds))
                .collect(Collectors.toList());
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        return result;
    }

    private List<RankingItem> toRankingItems(List<QuizAttempt> attempts) {
        List<RankingItem> result = attempts.stream().map(attempt -> {
            RankingItem item = baseItem(attempt.getUserId());
            item.setScore(attempt.getScore());
            item.setTotalScore(attempt.getTotalScore());
            item.setDurationSeconds(attempt.getDurationSeconds());
            item.setSubmittedAt(attempt.getSubmittedAt());
            return item;
        }).collect(Collectors.toList());
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }
        return result;
    }

    private RankingItem baseItem(Long userId) {
        User user = userMapper.selectById(userId);
        RankingItem item = new RankingItem();
        item.setUserId(userId);
        item.setNickname(user == null ? "" : user.getNickname());
        item.setAvatarUrl(user == null ? "" : user.getAvatarUrl());
        item.setScore(0);
        item.setTotalScore(0);
        item.setDurationSeconds(0);
        return item;
    }
}
