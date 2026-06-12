package com.dace.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.quiz.common.BusinessException;
import com.dace.quiz.dto.ApiModels.SpaceRequest;
import com.dace.quiz.entity.Space;
import com.dace.quiz.entity.SpaceMember;
import com.dace.quiz.mapper.SpaceMapper;
import com.dace.quiz.mapper.SpaceMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceMapper spaceMapper;
    private final SpaceMemberMapper memberMapper;
    private final GuardService guardService;

    public Space create(Long userId, String role, SpaceRequest request) {
        guardService.requireRole(role, "CREATOR");
        Space space = new Space();
        space.setCreatorId(userId);
        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setStatus("ACTIVE");
        space.setCreatedAt(LocalDateTime.now());
        space.setUpdatedAt(LocalDateTime.now());
        spaceMapper.insert(space);
        return space;
    }

    public Space update(Long userId, Long id, SpaceRequest request) {
        Space space = guardService.requireCreatorSpace(id, userId);
        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setUpdatedAt(LocalDateTime.now());
        spaceMapper.updateById(space);
        return space;
    }

    public void archive(Long userId, Long id) {
        Space space = guardService.requireCreatorSpace(id, userId);
        space.setStatus("ARCHIVED");
        space.setUpdatedAt(LocalDateTime.now());
        spaceMapper.updateById(space);
    }

    public List<Space> mySpaces(Long userId, String role) {
        if ("CREATOR".equals(role)) {
            return spaceMapper.selectList(new LambdaQueryWrapper<Space>()
                    .eq(Space::getCreatorId, userId)
                    .orderByDesc(Space::getCreatedAt));
        }
        if ("PARTICIPANT".equals(role)) {
            List<SpaceMember> members = memberMapper.selectList(new LambdaQueryWrapper<SpaceMember>()
                    .eq(SpaceMember::getUserId, userId)
                    .orderByDesc(SpaceMember::getJoinedAt));
            return members.stream().map(m -> spaceMapper.selectById(m.getSpaceId())).filter(s -> s != null).collect(Collectors.toList());
        }
        throw new BusinessException(403, "请先完成注册");
    }
}
