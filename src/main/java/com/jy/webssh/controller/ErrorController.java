package com.jy.webssh.controller;

import com.jy.webssh.exception.RequestCountOutRangeException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JungleLin
 * @date 2023/2/269:10
 */
@RestController
public class ErrorController {

    @RequestMapping("/error/frequent")
    public void requestFrequent() throws RequestCountOutRangeException {
        throw new RequestCountOutRangeException("Requests are too frequent, please try again later");
    }
}
