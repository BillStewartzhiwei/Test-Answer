package com.dace.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dace.common.BizException;
import com.dace.common.ErrorCode;
import com.dace.config.AuthContext;
import com.dace.config.CurrentUser;
import com.dace.dto.LoginRequest;
import com.dace.dto.RegisterRequest;
import com.dace.entity.User;
import com.dace.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final WechatService wechatService;

    public AuthService(UserMapper userMapper, WechatService wechatService) {
        this.userMapper = userMapper;
        this.wechatService = wechatService;
    }

    public Map<String, Object> login(LoginRequest request) {
        String openid = wechatService.codeToOpenid(request.getCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("openid", openid);
        if (user == null) {
            data.put("token", "openid:" + openid);
            data.put("need_register", true);
            return data;
        }
        data.put("token", userToken(user));
        data.put("need_register", false);
        data.put("user", userProfile(user));
        return data;
    }

    public Map<String, Object> register(RegisterRequest request) {
        CurrentUser current = AuthContext.getOptional();
        if (current == null || current.getOpenid() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }

        User existing = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, current.getOpenid()));
        if (existing != null) {
            if (!existing.getRole().equals(request.getRole())) {
                throw new BizException(ErrorCode.ROLE_LOCKED);
            }
            return session(existing);
        }

        User user = new User();
        user.setOpenid(current.getOpenid());
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setRole(request.getRole());
        user.setCreatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return session(user);
    }

    public Map<String, Object> profile() {
        User user = userMapper.selectById(AuthContext.get().getId());
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return userProfile(user);
    }

    public Map<String, Object> updateProfile(Map<String, Object> request) {
        CurrentUser current = AuthContext.get();
        User user = userMapper.selectById(current.getId());
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (request.containsKey("nickname")) {
            user.setNickname((String) request.get("nickname"));
        }
        if (request.containsKey("avatar_url")) {
            user.setAvatarUrl((String) request.get("avatar_url"));
        }
        if (request.containsKey("avatarUrl")) {
            user.setAvatarUrl((String) request.get("avatarUrl"));
        }
        userMapper.updateById(user);
        return userProfile(user);
    }

    private Map<String, Object> session(User user) {
        Map<String, Object> data = userProfile(user);
        data.put("token", userToken(user));
        return data;
    }

    private Map<String, Object> userProfile(User user) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("openid", user.getOpenid());
        data.put("nickname", user.getNickname());
        data.put("avatar_url", user.getAvatarUrl());
        data.put("role", user.getRole());
        return data;
    }

    private String userToken(User user) {
        return "user:" + user.getId() + ":" + user.getRole();
    }
}
