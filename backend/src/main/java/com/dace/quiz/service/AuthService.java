package com.dace.quiz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.quiz.common.BusinessException;
import com.dace.quiz.dto.ApiModels.AuthResult;
import com.dace.quiz.dto.ApiModels.LoginRequest;
import com.dace.quiz.dto.ApiModels.RegisterRequest;
import com.dace.quiz.entity.User;
import com.dace.quiz.mapper.UserMapper;
import com.dace.quiz.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public AuthResult login(LoginRequest request) {
        String openId = StringUtils.hasText(request.getOpenId()) ? request.getOpenId() : "mock-" + request.getCode();
        if (!StringUtils.hasText(openId) || openId.endsWith("null")) {
            throw new BusinessException("缺少微信登录凭证");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId, openId));
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname(request.getNickname());
            user.setAvatarUrl(request.getAvatarUrl());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        }
        return toAuthResult(user);
    }

    public AuthResult register(Long userId, RegisterRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (StringUtils.hasText(user.getRole())) {
            throw new BusinessException("角色注册后不可修改");
        }
        if (!"CREATOR".equals(request.getRole()) && !"PARTICIPANT".equals(request.getRole())) {
            throw new BusinessException("角色必须是 CREATOR 或 PARTICIPANT");
        }
        user.setRole(request.getRole());
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toAuthResult(user);
    }

    public User updateProfile(Long userId, RegisterRequest request) {
        User user = userMapper.selectById(userId);
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return user;
    }

    private AuthResult toAuthResult(User user) {
        AuthResult result = new AuthResult();
        result.setUserId(user.getId());
        result.setRole(user.getRole());
        result.setNickname(user.getNickname());
        result.setAvatarUrl(user.getAvatarUrl());
        result.setRegistered(StringUtils.hasText(user.getRole()));
        result.setToken(jwtUtil.createToken(user.getId(), user.getRole() == null ? "UNREGISTERED" : user.getRole()));
        return result;
    }
}
