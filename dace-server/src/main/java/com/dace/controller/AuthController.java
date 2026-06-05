package com.dace.controller;

import com.dace.common.ApiResponse;
import com.dace.dto.LoginRequest;
import com.dace.dto.RegisterRequest;
import com.dace.service.AuthService;
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
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/auth/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @GetMapping("/user/profile")
    public ApiResponse<Map<String, Object>> profile() {
        return ApiResponse.ok(authService.profile());
    }

    @PutMapping("/user/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(authService.updateProfile(request));
    }
}
