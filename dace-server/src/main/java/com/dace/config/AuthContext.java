package com.dace.config;

public final class AuthContext {
    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public static CurrentUser get() {
        CurrentUser user = CURRENT.get();
        return user == null ? new CurrentUser(1L, "creator") : user;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
