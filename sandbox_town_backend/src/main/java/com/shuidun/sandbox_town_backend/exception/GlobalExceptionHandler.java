package com.shuidun.sandbox_town_backend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.shuidun.sandbox_town_backend.bean.Response;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Response<?> exceptionHandler(Exception e) {
        e.printStackTrace();
        // log.error("e=\"{}\". Stack is :\n", e.getMessage(), e);
        return new Response<>(StatusCodeEnum.SERVER_ERROR);
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseBody
    public Response<?> exceptionHandler(NotLoginException e) {
        log.error("e=\"{}\". Stack is :\n", e.getMessage(), e);
        return new Response<>(StatusCodeEnum.UNAUTHORIZED);
    }

}