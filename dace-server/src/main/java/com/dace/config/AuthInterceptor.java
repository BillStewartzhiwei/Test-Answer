package com.dace.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            AuthContext.set(parseToken(auth.substring("Bearer ".length())));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private CurrentUser parseToken(String token) {
        if (token.startsWith("openid:")) {
            return new CurrentUser(null, null, token.substring("openid:".length()));
        }
        if (token.startsWith("user:")) {
            String[] parts = token.split(":");
            if (parts.length >= 3) {
                return new CurrentUser(Long.valueOf(parts[1]), parts[2]);
            }
        }
        return new CurrentUser(null, null, null);
    }
}
