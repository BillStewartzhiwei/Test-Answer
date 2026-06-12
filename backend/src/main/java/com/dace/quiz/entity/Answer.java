package com.dace.quiz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("answers")
public class Answer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long attemptId;
    private Long questionId;
    private String selectedOptionIds;
    private Boolean correct;
    private Integer score;
    private LocalDateTime createdAt;
}
