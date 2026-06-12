package com.dace.quiz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quiz_attempts")
public class QuizAttempt {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long quizId;
    private Long spaceId;
    private Long userId;
    private Integer score;
    private Integer totalScore;
    private Integer correctCount;
    private Integer questionCount;
    private Integer durationSeconds;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    @TableLogic
    private Integer deleted;
}
