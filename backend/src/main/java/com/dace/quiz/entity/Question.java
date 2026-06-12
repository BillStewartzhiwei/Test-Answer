package com.dace.quiz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("questions")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long quizId;
    private String type;
    private String title;
    private Integer score;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
