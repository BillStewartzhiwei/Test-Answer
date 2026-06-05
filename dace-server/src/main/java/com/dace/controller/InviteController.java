package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.CreateInviteRequest;
import com.dace.dto.JoinInviteRequest;
import com.dace.service.InviteService;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class InviteController {
    private final InviteService inviteService;

    public InviteController(InviteService inviteService) {
        this.inviteService = inviteService;
    }

    @PostMapping("/spaces/{id}/invites")
    public ApiResponse<Map<String, Object>> create(@PathVariable Long id, @RequestBody CreateInviteRequest request) {
        return ApiResponse.ok(inviteService.create(id, request));
    }

    @GetMapping("/spaces/{id}/invites")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable Long id) {
        return ApiResponse.ok(inviteService.list(id));
    }

    @DeleteMapping("/spaces/{id}/invites/{codeId}")
    public ApiResponse<Map<String, Object>> disable(@PathVariable Long id, @PathVariable Long codeId) {
        return ApiResponse.ok(inviteService.disable(id, codeId));
    }

    @PostMapping("/invites/join")
    public ApiResponse<Map<String, Object>> join(@Valid @RequestBody JoinInviteRequest request) {
        return ApiResponse.ok(inviteService.join(request));
    }
}
