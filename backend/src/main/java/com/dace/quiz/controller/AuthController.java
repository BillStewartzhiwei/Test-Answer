package com.dace.quiz.controller;

import com.dace.quiz.common.ApiResponse;
import com.dace.quiz.dto.ApiModels.AuthResult;
import com.dace.quiz.dto.ApiModels.LoginRequest;
import com.dace.quiz.dto.ApiModels.RegisterRequest;
import com.dace.quiz.entity.User;
import com.dace.quiz.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AuthResult> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ApiResponse<AuthResult> register(HttpServletRequest servletRequest, @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register((Long) servletRequest.getAttribute("userId"), request));
    }

    @PutMapping("/profile")
    public ApiResponse<User> profile(HttpServletRequest servletRequest, @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.updateProfile((Long) servletRequest.getAttribute("userId"), request));
    }
}
