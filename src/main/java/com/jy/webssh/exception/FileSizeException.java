package com.jy.webssh.exception;

/**
 * 文件大小限制在1MB
 * 用于抛出文件大小超出范围的错误
 *
 * @author JungleLin
 * @date 2023/2/265:25
 */
public class FileSizeException extends Exception {
    public FileSizeException(String message) {
        super(message);
    }
}
