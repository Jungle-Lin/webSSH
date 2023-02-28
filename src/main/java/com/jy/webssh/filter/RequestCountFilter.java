package com.jy.webssh.filter;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author JungleLin
 * @date 2023/2/268:47
 */
@Component
public class RequestCountFilter implements Filter {
    private static final long UNIT_TIME = 60;
    private static final int MAX_COUNT_UNIT_TIME = 20;
    private static final ExpiringMap<String, Integer> map = ExpiringMap.builder().variableExpiration().build();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Credentials", "true");
        Integer count = map.getOrDefault(request.getRemoteAddr(), 0);
        if (count >= MAX_COUNT_UNIT_TIME) {
            request.getRequestDispatcher("/error/frequent").forward(request, response);
        } else if (count == 0) {
            /**
             * map.put(
             * key, value , ExpirationPolicy(过期策略),duration(持续时间), TimeUnit(时间格式: 日、时、分、秒、毫秒)
             * )
             */
            map.put(request.getRemoteAddr(), count + 1, ExpirationPolicy.CREATED, UNIT_TIME, TimeUnit.SECONDS);
        } else {
            map.put(request.getRemoteAddr(), count + 1);
        }
        filterChain.doFilter(request, response);
    }
}
