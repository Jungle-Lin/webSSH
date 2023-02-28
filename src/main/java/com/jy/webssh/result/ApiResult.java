package com.jy.webssh.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @author JungleLin
 * @date 2023/2/263:29
 */
@Data
public class ApiResult<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public static <T> ApiResult<T> success(T data) {
        return ApiResult.success(StatusCodeEnum.CODE200.getMsg(), data);
    }

    public static <T> ApiResult<T> success(String msg, T data) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(StatusCodeEnum.CODE200.getCode());
        apiResult.setMsg(msg);
        apiResult.setData(data);
        return apiResult;
    }

    public static <T> ApiResult<T> fail(Integer code, String msg) {
        ApiResult<T> apiResult = new ApiResult<>();
        apiResult.setCode(code);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
