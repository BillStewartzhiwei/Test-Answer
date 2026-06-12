package com.dace.quiz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("invite_codes")
public class InviteCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spaceId;
    private Long creatorId;
    private String code;
    private LocalDateTime expireAt;
    private Integer maxUses;
    private Integer usedCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
