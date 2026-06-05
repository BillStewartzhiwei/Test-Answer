package com.dace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dace.common.BizException;
import com.dace.common.ErrorCode;
import com.dace.config.AuthContext;
import com.dace.dto.CreateInviteRequest;
import com.dace.dto.JoinInviteRequest;
import com.dace.entity.InviteCode;
import com.dace.entity.Space;
import com.dace.entity.SpaceMember;
import com.dace.mapper.InviteCodeMapper;
import com.dace.mapper.SpaceMapper;
import com.dace.mapper.SpaceMemberMapper;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteService {
    private static final char[] CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private final SecureRandom random = new SecureRandom();
    private final InviteCodeMapper inviteCodeMapper;
    private final SpaceMapper spaceMapper;
    private final SpaceMemberMapper spaceMemberMapper;
    private final PermissionService permissionService;
    private final SpaceService spaceService;

    public InviteService(InviteCodeMapper inviteCodeMapper, SpaceMapper spaceMapper, SpaceMemberMapper spaceMemberMapper,
                         PermissionService permissionService, SpaceService spaceService) {
        this.inviteCodeMapper = inviteCodeMapper;
        this.spaceMapper = spaceMapper;
        this.spaceMemberMapper = spaceMemberMapper;
        this.permissionService = permissionService;
        this.spaceService = spaceService;
    }

    public Map<String, Object> create(Long spaceId, CreateInviteRequest request) {
        permissionService.requireOwner(spaceId);
        InviteCode invite = new InviteCode();
        invite.setSpaceId(spaceId);
        invite.setCode(uniqueCode());
        invite.setMaxUses(request.getMaxUses() == null ? 50 : request.getMaxUses());
        invite.setUsedCount(0);
        invite.setExpireAt(LocalDateTime.now().plusDays(request.getExpireDays() == null ? 7 : request.getExpireDays()));
        invite.setIsActive(1);
        invite.setCreatedAt(LocalDateTime.now());
        inviteCodeMapper.insert(invite);
        return toMap(invite);
    }

    public List<Map<String, Object>> list(Long spaceId) {
        permissionService.requireOwner(spaceId);
        return inviteCodeMapper.selectList(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getSpaceId, spaceId)
                .orderByDesc(InviteCode::getCreatedAt))
            .stream().map(this::toMap).collect(Collectors.toList());
    }

    public Map<String, Object> disable(Long spaceId, Long codeId) {
        permissionService.requireOwner(spaceId);
        InviteCode invite = inviteCodeMapper.selectById(codeId);
        if (invite == null || !invite.getSpaceId().equals(spaceId)) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        invite.setIsActive(0);
        inviteCodeMapper.updateById(invite);
        return toMap(invite);
    }

    @Transactional
    public Map<String, Object> join(JoinInviteRequest request) {
        permissionService.requireRole("participant");
        InviteCode invite = inviteCodeMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
            .eq(InviteCode::getCode, request.getCode()));
        if (invite == null || Integer.valueOf(0).equals(invite.getIsActive())) {
            throw new BizException(ErrorCode.INVITE_INVALID);
        }
        if (invite.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.INVITE_EXPIRED);
        }
        if (invite.getUsedCount() >= invite.getMaxUses()) {
            throw new BizException(ErrorCode.INVITE_LIMIT_REACHED);
        }
        Long userId = AuthContext.get().getId();
        Long existing = spaceMemberMapper.selectCount(new LambdaQueryWrapper<SpaceMember>()
            .eq(SpaceMember::getSpaceId, invite.getSpaceId())
            .eq(SpaceMember::getUserId, userId));
        if (existing > 0) {
            throw new BizException(ErrorCode.ALREADY_JOINED);
        }

        SpaceMember member = new SpaceMember();
        member.setSpaceId(invite.getSpaceId());
        member.setUserId(userId);
        member.setInviteCodeId(invite.getId());
        member.setJoinedAt(LocalDateTime.now());
        spaceMemberMapper.insert(member);

        inviteCodeMapper.update(null, new LambdaUpdateWrapper<InviteCode>()
            .eq(InviteCode::getId, invite.getId())
            .set(InviteCode::getUsedCount, invite.getUsedCount() + 1));

        Space space = spaceMapper.selectById(invite.getSpaceId());
        spaceMapper.update(null, new LambdaUpdateWrapper<Space>()
            .eq(Space::getId, space.getId())
            .set(Space::getMemberCount, space.getMemberCount() + 1));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("space", spaceService.toMap(spaceMapper.selectById(space.getId())));
        return data;
    }

    private String uniqueCode() {
        for (int i = 0; i < 20; i++) {
            String code = randomCode();
            Long count = inviteCodeMapper.selectCount(new LambdaQueryWrapper<InviteCode>().eq(InviteCode::getCode, code));
            if (count == 0) {
                return code;
            }
        }
        throw new BizException(ErrorCode.PARAM_ERROR);
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS[random.nextInt(CODE_CHARS.length)]);
        }
        return sb.toString();
    }

    private Map<String, Object> toMap(InviteCode invite) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", invite.getId());
        data.put("space_id", invite.getSpaceId());
        data.put("code", invite.getCode());
        data.put("max_uses", invite.getMaxUses());
        data.put("used_count", invite.getUsedCount());
        data.put("expire_at", invite.getExpireAt());
        data.put("is_active", invite.getIsActive());
        data.put("created_at", invite.getCreatedAt());
        return data;
    }
}
