package com.mobe.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class RequestLogInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);
    private static final String START_TIME = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        log.info("[HTTP-REQ] {} {}{}", method, uri, queryString == null ? "" : "?" + queryString);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        Object startAttr = request.getAttribute(START_TIME);
        long cost = 0L;
        if (startAttr instanceof Long startTime) {
            cost = System.currentTimeMillis() - startTime;
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        if (ex != null) {
            log.error("[HTTP-RESP] {} {} | status={} | cost={}ms | error={}",
                    method, uri, status, cost, ex.getMessage(), ex);
        } else {
            log.info("[HTTP-RESP] {} {} | status={} | cost={}ms",
                    method, uri, status, cost);
        }
    }
}