package com.shuidun.sandbox_town_backend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public RestResponse<?> exceptionHandler(Exception e) {
        log.error("e=\"{}\". Stack is :\n", e.getMessage(), e);
        return new RestResponse<>(StatusCodeEnum.SERVER_ERROR);
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    public RestResponse<?> exceptionHandler(NotLoginException e) {
        log.error("e=\"{}\". Stack is :\n", e.getMessage(), e);
        return new RestResponse<>(StatusCodeEnum.NO_PERMISSION);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public RestResponse<?> exceptionHandler(BusinessException e) {
        log.error("e=\"{}\". Stack is :\n", e.getMessage(), e);
        return new RestResponse<>(e.getStatusCode());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public RestResponse<?> exceptionHandler(HttpRequestMethodNotSupportedException e) {
        log.error("e=\"{}\". Stack is :\n", e.getMessage(), e);
        return new RestResponse<>(StatusCodeEnum.REQUEST_METHOD_NOT_SUPPORTED);
    }

}