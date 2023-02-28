package com.jy.webssh.result;

import com.jcraft.jsch.JSchException;
import com.jy.webssh.exception.FileSizeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author JungleLin
 * @date 2023/2/263:34
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获jsch错误
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(JSchException.class)
    public ApiResult<String> handle(JSchException e) {
        return ApiResult.fail(StatusCodeEnum.CODE500.getCode(), StatusCodeEnum.CODE500.getMsg() + ": " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResult<String> handle(NoHandlerFoundException e) {
        return ApiResult.fail(StatusCodeEnum.CODE500.getCode(), StatusCodeEnum.CODE500.getMsg() + ": " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResult<String> handle(Exception e) {
        return ApiResult.fail(StatusCodeEnum.CODE500.getCode(), StatusCodeEnum.CODE500.getMsg() + ": " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(FileSizeException.class)
    public ApiResult<String> handle(FileSizeException e) {
        return ApiResult.fail(StatusCodeEnum.CODE500.getCode(), StatusCodeEnum.CODE500.getMsg() + ": " + e.getMessage());
    }
}
