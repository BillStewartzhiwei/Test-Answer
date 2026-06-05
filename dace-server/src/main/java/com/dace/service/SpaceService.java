package com.dace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.config.AuthContext;
import com.dace.config.CurrentUser;
import com.dace.dto.CreateSpaceRequest;
import com.dace.entity.Space;
import com.dace.entity.SpaceMember;
import com.dace.mapper.SpaceMapper;
import com.dace.mapper.SpaceMemberMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SpaceService {
    private final SpaceMapper spaceMapper;
    private final SpaceMemberMapper spaceMemberMapper;
    private final PermissionService permissionService;

    public SpaceService(SpaceMapper spaceMapper, SpaceMemberMapper spaceMemberMapper, PermissionService permissionService) {
        this.spaceMapper = spaceMapper;
        this.spaceMemberMapper = spaceMemberMapper;
        this.permissionService = permissionService;
    }

    public Map<String, Object> create(CreateSpaceRequest request) {
        CurrentUser user = permissionService.requireRole("creator");
        Space space = new Space();
        space.setOwnerId(user.getId());
        space.setName(request.getName());
        space.setDescription(request.getDescription());
        space.setMemberCount(0);
        space.setQuizCount(0);
        space.setStatus(1);
        space.setCreatedAt(LocalDateTime.now());
        spaceMapper.insert(space);
        return toMap(space);
    }

    public List<Map<String, Object>> list() {
        CurrentUser user = AuthContext.get();
        if ("creator".equals(user.getRole())) {
            return spaceMapper.selectList(new LambdaQueryWrapper<Space>()
                    .eq(Space::getOwnerId, user.getId())
                    .eq(Space::getStatus, 1)
                    .orderByDesc(Space::getCreatedAt))
                .stream().map(this::toMap).collect(Collectors.toList());
        }

        List<Long> ids = spaceMemberMapper.selectList(new LambdaQueryWrapper<SpaceMember>()
                .eq(SpaceMember::getUserId, user.getId()))
            .stream().map(SpaceMember::getSpaceId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return List.of();
        }
        return spaceMapper.selectList(new LambdaQueryWrapper<Space>()
                .in(Space::getId, ids)
                .eq(Space::getStatus, 1)
                .orderByDesc(Space::getCreatedAt))
            .stream().map(this::toMap).collect(Collectors.toList());
    }

    public Map<String, Object> detail(Long id) {
        return toMap(permissionService.requireMember(id));
    }

    public Map<String, Object> update(Long id, CreateSpaceRequest request) {
        Space space = permissionService.requireOwner(id);
        space.setName(request.getName());
        space.setDescription(request.getDescription());
        spaceMapper.updateById(space);
        return toMap(space);
    }

    public Map<String, Object> archive(Long id) {
        Space space = permissionService.requireOwner(id);
        space.setStatus(0);
        spaceMapper.updateById(space);
        return toMap(space);
    }

    public Map<String, Object> toMap(Space space) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", space.getId());
        data.put("owner_id", space.getOwnerId());
        data.put("name", space.getName());
        data.put("description", space.getDescription());
        data.put("member_count", space.getMemberCount());
        data.put("quiz_count", space.getQuizCount());
        data.put("status", space.getStatus());
        data.put("created_at", space.getCreatedAt());
        return data;
    }
}
