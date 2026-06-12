package com.dace.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.quiz.common.BusinessException;
import com.dace.quiz.entity.Space;
import com.dace.quiz.entity.SpaceMember;
import com.dace.quiz.mapper.SpaceMapper;
import com.dace.quiz.mapper.SpaceMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuardService {
    private final SpaceMapper spaceMapper;
    private final SpaceMemberMapper memberMapper;

    public void requireRole(String actual, String expected) {
        if (!expected.equals(actual)) {
            throw new BusinessException(403, "无权限");
        }
    }

    public Space requireCreatorSpace(Long spaceId, Long userId) {
        Space space = spaceMapper.selectById(spaceId);
        if (space == null || !"ACTIVE".equals(space.getStatus())) {
            throw new BusinessException(404, "空间不存在");
        }
        if (!userId.equals(space.getCreatorId())) {
            throw new BusinessException(403, "只能操作自己创建的空间");
        }
        return space;
    }

    public void requireSpaceMember(Long spaceId, Long userId) {
        Long count = memberMapper.selectCount(new LambdaQueryWrapper<SpaceMember>()
                .eq(SpaceMember::getSpaceId, spaceId)
                .eq(SpaceMember::getUserId, userId));
        if (count == 0) {
            throw new BusinessException(403, "未加入该空间");
        }
    }
}
