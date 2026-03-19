package com.mobe.user.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.user.dto.UserSessionResponse;
import com.mobe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 * <p>
 * 功能：处理用户会话相关的请求，包括获取会话列表和下线会话
 * 说明：使用 @RestController 注解标记为 REST 控制器，@RequestMapping 指定基础路径
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    /**
     * 用户服务
     */
    private final UserService userService;

    /**
     * 构造方法
     * <p>
     * 功能：注入用户服务
     * @param userService 用户服务实例
     */
    public SessionController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取会话列表
     * <p>
     * 功能：获取当前用户的所有会话列表
     * @param request HTTP 请求对象
     * @return ApiResponse<List<UserSessionResponse>> 会话列表
     */
    @GetMapping
    public ApiResponse<List<UserSessionResponse>> list(HttpServletRequest request) {
        log.info("收到获取会话列表请求");
        List<UserSessionResponse> data = userService.getSessionList(request);
        return ApiResponse.success("获取会话列表成功", data);
    }

    /**
     * 下线会话
     * <p>
     * 功能：下线指定的会话
     * @param sessionId 会话ID
     * @param request HTTP 请求对象
     * @return ApiResponse<String> 下线结果
     */
    @DeleteMapping("/{sessionId}")
    public ApiResponse<String> delete(@PathVariable String sessionId, HttpServletRequest request) {
        log.info("收到下线会话请求，sessionId={}", sessionId);
        String result = userService.deleteSession(sessionId, request);
        return ApiResponse.success(result);
    }
}