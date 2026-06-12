package com.dace.quiz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quizzes")
public class Quiz {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private Long creatorId;
    private String title;
    private Integer timeLimitMinutes;
    private Integer totalScore;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
