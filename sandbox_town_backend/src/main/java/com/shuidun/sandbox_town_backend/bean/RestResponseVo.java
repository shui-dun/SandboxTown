package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * restful接口返回的数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestResponseVo<T> {
    private int code;
    private String msg;
    @Nullable
    private T data;

    public RestResponseVo(StatusCodeEnum codeEnum, @Nullable T data) {
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
        this.data = data;
    }

    public RestResponseVo(StatusCodeEnum codeEnum) {
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
    }
}
