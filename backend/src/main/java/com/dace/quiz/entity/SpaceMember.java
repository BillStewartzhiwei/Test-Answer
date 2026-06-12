package com.dace.quiz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("space_members")
public class SpaceMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private Long userId;
    private LocalDateTime joinedAt;
    @TableLogic
    private Integer deleted;
}
