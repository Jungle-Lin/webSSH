package com.jy.webssh.exception;

/**
 * @author JungleLin
 * @date 2023/2/268:55
 */
public class RequestCountOutRangeException extends Exception{
    public RequestCountOutRangeException(String message){
        super(message);
    }
}
