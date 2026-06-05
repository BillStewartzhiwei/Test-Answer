package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.LoginRequest;
import com.dace.dto.RegisterRequest;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class AuthController {
    @PostMapping("/auth/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(Map.of(
            "token", "dev-token",
            "openid", "openid-from-wechat-code-placeholder"
        ));
    }

    @PostMapping("/auth/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(Map.of(
            "id", 1,
            "role", request.getRole(),
            "nickname", request.getNickname()
        ));
    }

    @GetMapping("/user/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.ok(Map.of("id", 1, "nickname", "dev user", "role", "creator"));
    }

    @PutMapping("/user/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(request);
    }
}
