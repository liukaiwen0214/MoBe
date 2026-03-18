package com.mobe.user.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.user.dto.UserSessionResponse;
import com.mobe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final UserService userService;

    public SessionController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserSessionResponse>> list(HttpServletRequest request) {
        log.info("收到获取会话列表请求");
        List<UserSessionResponse> data = userService.getSessionList(request);
        return ApiResponse.success("获取会话列表成功", data);
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<String> delete(@PathVariable String sessionId, HttpServletRequest request) {
        log.info("收到下线会话请求，sessionId={}", sessionId);
        String result = userService.deleteSession(sessionId, request);
        return ApiResponse.success(result);
    }
}