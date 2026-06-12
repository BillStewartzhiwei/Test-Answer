package com.dace.quiz.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class ApiModels {
    @Data
    public static class LoginRequest {
        private String code;
        private String openId;
        private String nickname;
        private String avatarUrl;
    }

    @Data
    public static class RegisterRequest {
        private String role;
        private String nickname;
        private String avatarUrl;
    }

    @Data
    public static class AuthResult {
        private String token;
        private Long userId;
        private String role;
        private String nickname;
        private String avatarUrl;
        private Boolean registered;
    }

    @Data
    public static class SpaceRequest {
        private String name;
        private String description;
    }

    @Data
    public static class InviteCreateRequest {
        private Integer validDays;
        private Integer maxUses;
    }

    @Data
    public static class JoinRequest {
        private String code;
    }

    @Data
    public static class QuizRequest {
        private Long spaceId;
        private String title;
        private Integer timeLimitMinutes;
        private List<QuestionRequest> questions;
    }

    @Data
    public static class QuestionRequest {
        private Long id;
        private String type;
        private String title;
        private Integer score;
        private Integer sortOrder;
        private List<OptionRequest> options;
    }

    @Data
    public static class OptionRequest {
        private Long id;
        private String content;
        private Boolean correct;
        private Integer sortOrder;
    }

    @Data
    public static class SubmitAttemptRequest {
        private Integer durationSeconds;
        private List<AnswerRequest> answers;
    }

    @Data
    public static class AnswerRequest {
        private Long questionId;
        private List<Long> selectedOptionIds;
    }

    @Data
    public static class AttemptResult {
        private Long attemptId;
        private Integer score;
        private Integer totalScore;
        private Integer correctCount;
        private Integer questionCount;
        private Integer durationSeconds;
        private Double accuracy;
        private Integer rank;
        private List<AnswerDetail> details;
    }

    @Data
    public static class AnswerDetail {
        private Long questionId;
        private String title;
        private Integer score;
        private Boolean correct;
        private List<Long> selectedOptionIds;
        private List<Long> correctOptionIds;
    }

    @Data
    public static class RankingItem {
        private Integer rank;
        private Long userId;
        private String nickname;
        private String avatarUrl;
        private Integer score;
        private Integer totalScore;
        private Integer durationSeconds;
        private LocalDateTime submittedAt;
    }
}
