package com.mobe.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器
 * <p>
 * 功能：记录 HTTP 请求的详细信息，包括请求方法、路径、响应状态和处理时间
 * 说明：实现 HandlerInterceptor 接口，用于拦截所有 HTTP 请求
 */
public class RequestLogInterceptor implements HandlerInterceptor {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(RequestLogInterceptor.class);
    
    /**
     * 请求开始时间属性名
     */
    private static final String START_TIME = "requestStartTime";

    /**
     * 请求处理前的回调方法
     * <p>
     * 功能：记录请求开始时间和请求信息
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param handler 处理器对象
     * @return boolean 是否继续处理请求
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 记录请求开始时间
        request.setAttribute(START_TIME, System.currentTimeMillis());

        // 获取请求方法、路径和查询参数
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();

        // 记录请求信息
        log.info("[HTTP-REQ] {} {}{}", method, uri, queryString == null ? "" : "?" + queryString);
        return true;
    }

    /**
     * 请求处理完成后的回调方法
     * <p>
     * 功能：计算请求处理时间并记录响应信息
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param handler 处理器对象
     * @param ex 异常对象，如果有的话
     */
    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // 获取请求开始时间
        Object startAttr = request.getAttribute(START_TIME);
        long cost = 0L;
        if (startAttr instanceof Long startTime) {
            // 计算处理时间
            cost = System.currentTimeMillis() - startTime;
        }

        // 获取请求方法、路径和响应状态
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // 记录响应信息
        if (ex != null) {
            log.error("[HTTP-RESP] {} {} | status={} | cost={}ms | error={}",
                    method, uri, status, cost, ex.getMessage(), ex);
        } else {
            log.info("[HTTP-RESP] {} {} | status={} | cost={}ms",
                    method, uri, status, cost);
        }
    }
}