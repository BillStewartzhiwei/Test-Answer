package com.dace.common;

public enum ErrorCode {
    PARAM_ERROR(1001, "invalid parameter"),
    UNAUTHORIZED(1002, "not logged in"),
    FORBIDDEN(1003, "forbidden"),
    INVITE_INVALID(2001, "invite code is invalid"),
    INVITE_EXPIRED(2002, "invite code is expired"),
    INVITE_LIMIT_REACHED(2003, "invite code reached max uses"),
    ALREADY_JOINED(2004, "already joined this space"),
    ALREADY_SUBMITTED(3001, "quiz can only be submitted once");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
