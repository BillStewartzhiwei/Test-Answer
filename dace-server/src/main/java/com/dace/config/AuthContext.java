package com.dace.config;

import com.dace.common.BizException;
import com.dace.common.ErrorCode;

public final class AuthContext {
    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public static CurrentUser get() {
        CurrentUser user = CURRENT.get();
        if (user == null || !user.isRegistered()) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    public static CurrentUser getOptional() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
