package com.dace.config;

public class CurrentUser {
    private final Long id;
    private final String role;
    private final String openid;

    public CurrentUser(Long id, String role) {
        this(id, role, null);
    }

    public CurrentUser(Long id, String role, String openid) {
        this.id = id;
        this.role = role;
        this.openid = openid;
    }

    public Long getId() { return id; }
    public String getRole() { return role; }
    public String getOpenid() { return openid; }
    public boolean isRegistered() { return id != null; }
}
