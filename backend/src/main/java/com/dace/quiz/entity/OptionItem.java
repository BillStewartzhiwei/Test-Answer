package com.dace.quiz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("options")
public class OptionItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String content;
    private Boolean correct;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
