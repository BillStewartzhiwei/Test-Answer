package com.dace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.common.BizException;
import com.dace.common.ErrorCode;
import com.dace.config.AuthContext;
import com.dace.config.CurrentUser;
import com.dace.entity.Space;
import com.dace.entity.SpaceMember;
import com.dace.mapper.SpaceMapper;
import com.dace.mapper.SpaceMemberMapper;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    private final SpaceMapper spaceMapper;
    private final SpaceMemberMapper spaceMemberMapper;

    public PermissionService(SpaceMapper spaceMapper, SpaceMemberMapper spaceMemberMapper) {
        this.spaceMapper = spaceMapper;
        this.spaceMemberMapper = spaceMemberMapper;
    }

    public CurrentUser requireRole(String role) {
        CurrentUser user = AuthContext.get();
        if (!role.equals(user.getRole())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    public Space requireSpace(Long spaceId) {
        Space space = spaceMapper.selectById(spaceId);
        if (space == null || Integer.valueOf(0).equals(space.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return space;
    }

    public Space requireOwner(Long spaceId) {
        CurrentUser user = AuthContext.get();
        Space space = requireSpace(spaceId);
        if (!space.getOwnerId().equals(user.getId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return space;
    }

    public Space requireMember(Long spaceId) {
        CurrentUser user = AuthContext.get();
        Space space = requireSpace(spaceId);
        if (space.getOwnerId().equals(user.getId())) {
            return space;
        }
        Long count = spaceMemberMapper.selectCount(new LambdaQueryWrapper<SpaceMember>()
            .eq(SpaceMember::getSpaceId, spaceId)
            .eq(SpaceMember::getUserId, user.getId()));
        if (count == 0) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return space;
    }
}
