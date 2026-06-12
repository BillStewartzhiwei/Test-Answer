package com.dace.quiz.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        return ApiResponse.fail(400, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handle(Exception e) {
        return ApiResponse.fail(500, e.getMessage());
    }
}
