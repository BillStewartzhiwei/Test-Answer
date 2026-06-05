package com.dace.config;

public class CurrentUser {
    private final Long id;
    private final String role;

    public CurrentUser(Long id, String role) {
        this.id = id;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getRole() { return role; }
}
