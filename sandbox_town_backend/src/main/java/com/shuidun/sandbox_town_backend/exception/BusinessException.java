package com.shuidun.sandbox_town_backend.exception;

import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;

public class BusinessException extends RuntimeException {
    private final StatusCodeEnum statusCode;

    public BusinessException(StatusCodeEnum statusCode) {
        super(statusCode.getMsg());
        this.statusCode = statusCode;
    }

    public StatusCodeEnum getStatusCode() {
        return statusCode;
    }
}