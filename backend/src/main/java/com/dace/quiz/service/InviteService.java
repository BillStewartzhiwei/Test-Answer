package com.dace.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.quiz.common.BusinessException;
import com.dace.quiz.dto.ApiModels.InviteCreateRequest;
import com.dace.quiz.dto.ApiModels.JoinRequest;
import com.dace.quiz.entity.InviteCode;
import com.dace.quiz.entity.SpaceMember;
import com.dace.quiz.mapper.InviteCodeMapper;
import com.dace.quiz.mapper.SpaceMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();
    private final InviteCodeMapper inviteMapper;
    private final SpaceMemberMapper memberMapper;
    private final GuardService guardService;

    public InviteCode create(Long userId, Long spaceId, InviteCreateRequest request) {
        guardService.requireCreatorSpace(spaceId, userId);
        InviteCode invite = new InviteCode();
        invite.setSpaceId(spaceId);
        invite.setCreatorId(userId);
        invite.setCode(nextCode());
        invite.setExpireAt(LocalDateTime.now().plusDays(request.getValidDays() == null ? 7 : request.getValidDays()));
        invite.setMaxUses(request.getMaxUses() == null ? 50 : request.getMaxUses());
        invite.setUsedCount(0);
        invite.setStatus("ACTIVE");
        invite.setCreatedAt(LocalDateTime.now());
        invite.setUpdatedAt(LocalDateTime.now());
        inviteMapper.insert(invite);
        return invite;
    }

    public List<InviteCode> list(Long userId, Long spaceId) {
        guardService.requireCreatorSpace(spaceId, userId);
        return inviteMapper.selectList(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getSpaceId, spaceId)
                .orderByDesc(InviteCode::getCreatedAt));
    }

    public void disable(Long userId, Long inviteId) {
        InviteCode invite = inviteMapper.selectById(inviteId);
        if (invite == null) {
            throw new BusinessException(404, "邀请码不存在");
        }
        guardService.requireCreatorSpace(invite.getSpaceId(), userId);
        invite.setStatus("DISABLED");
        invite.setUpdatedAt(LocalDateTime.now());
        inviteMapper.updateById(invite);
    }

    public void delete(Long userId, Long inviteId) {
        InviteCode invite = inviteMapper.selectById(inviteId);
        if (invite == null) {
            throw new BusinessException(404, "邀请码不存在");
        }
        guardService.requireCreatorSpace(invite.getSpaceId(), userId);
        inviteMapper.deleteById(inviteId);
    }

    @Transactional
    public SpaceMember join(Long userId, String role, JoinRequest request) {
        guardService.requireRole(role, "PARTICIPANT");
        InviteCode invite = inviteMapper.selectOne(new LambdaQueryWrapper<InviteCode>()
                .eq(InviteCode::getCode, request.getCode()));
        if (invite == null || !"ACTIVE".equals(invite.getStatus())) {
            throw new BusinessException("邀请码无效");
        }
        if (invite.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("邀请码已过期");
        }
        if (invite.getUsedCount() >= invite.getMaxUses()) {
            throw new BusinessException("邀请码使用次数已满");
        }
        Long joined = memberMapper.selectCount(new LambdaQueryWrapper<SpaceMember>()
                .eq(SpaceMember::getSpaceId, invite.getSpaceId())
                .eq(SpaceMember::getUserId, userId));
        if (joined > 0) {
            throw new BusinessException("已加入该空间");
        }
        SpaceMember member = new SpaceMember();
        member.setSpaceId(invite.getSpaceId());
        member.setUserId(userId);
        member.setJoinedAt(LocalDateTime.now());
        memberMapper.insert(member);
        invite.setUsedCount(invite.getUsedCount() + 1);
        invite.setUpdatedAt(LocalDateTime.now());
        inviteMapper.updateById(invite);
        return member;
    }

    private String nextCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
            }
            code = sb.toString();
        } while (inviteMapper.selectCount(new LambdaQueryWrapper<InviteCode>().eq(InviteCode::getCode, code)) > 0);
        return code;
    }
}
