package com.jy.webssh.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author JungleLin
 * @date 2023/2/263:25
 */
@AllArgsConstructor
@Getter
public enum StatusCodeEnum {
    CODE200(200, "success"),
    CODE999(999, "fail"),
    CODE413(413, "file to large"),
    CODE500(500, "ssh fail");

    private final Integer code;
    private final String msg;
}
