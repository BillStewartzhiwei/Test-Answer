package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.CreateInviteRequest;
import com.dace.dto.JoinInviteRequest;
import java.time.LocalDateTime;
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
    @PostMapping("/spaces/{id}/invites")
    public ApiResponse<Map<String, Object>> create(@PathVariable Long id, @RequestBody CreateInviteRequest request) {
        return ApiResponse.ok(Map.of(
            "space_id", id,
            "code", "A3X9K2",
            "max_uses", request.getMaxUses(),
            "expire_at", LocalDateTime.now().plusDays(request.getExpireDays()).toString()
        ));
    }

    @GetMapping("/spaces/{id}/invites")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable Long id) {
        return ApiResponse.ok(List.of());
    }

    @DeleteMapping("/spaces/{id}/invites/{codeId}")
    public ApiResponse<Map<String, Object>> disable(@PathVariable Long id, @PathVariable Long codeId) {
        return ApiResponse.ok(Map.of("space_id", id, "code_id", codeId, "is_active", 0));
    }

    @PostMapping("/invites/join")
    public ApiResponse<Map<String, Object>> join(@Valid @RequestBody JoinInviteRequest request) {
        return ApiResponse.ok(Map.of("space", Map.of("id", 1, "name", "Demo Space", "owner_name", "creator")));
    }
}
