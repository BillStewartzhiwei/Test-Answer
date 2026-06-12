package com.dace.quiz.controller;

import com.dace.quiz.common.ApiResponse;
import com.dace.quiz.dto.ApiModels.InviteCreateRequest;
import com.dace.quiz.dto.ApiModels.JoinRequest;
import com.dace.quiz.entity.InviteCode;
import com.dace.quiz.entity.SpaceMember;
import com.dace.quiz.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;

    @GetMapping("/spaces/{spaceId}/invites")
    public ApiResponse<List<InviteCode>> list(HttpServletRequest request, @PathVariable Long spaceId) {
        return ApiResponse.ok(inviteService.list(userId(request), spaceId));
    }

    @PostMapping("/spaces/{spaceId}/invites")
    public ApiResponse<InviteCode> create(HttpServletRequest request, @PathVariable Long spaceId, @RequestBody InviteCreateRequest body) {
        return ApiResponse.ok(inviteService.create(userId(request), spaceId, body));
    }

    @PostMapping("/invites/{inviteId}/disable")
    public ApiResponse<Void> disable(HttpServletRequest request, @PathVariable Long inviteId) {
        inviteService.disable(userId(request), inviteId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/invites/{inviteId}/delete")
    public ApiResponse<Void> delete(HttpServletRequest request, @PathVariable Long inviteId) {
        inviteService.delete(userId(request), inviteId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/invites/join")
    public ApiResponse<SpaceMember> join(HttpServletRequest request, @RequestBody JoinRequest body) {
        return ApiResponse.ok(inviteService.join(userId(request), role(request), body));
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private String role(HttpServletRequest request) {
        return (String) request.getAttribute("role");
    }
}
