package com.dace.dto;

import javax.validation.constraints.NotBlank;

public class JoinInviteRequest {
    @NotBlank
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
