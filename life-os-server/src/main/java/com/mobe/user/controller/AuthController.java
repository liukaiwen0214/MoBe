package com.mobe.user.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.user.dto.*;
import com.mobe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("收到用户注册请求，username={}, email={}", request.getUsername(), request.getEmail());

        String result = userService.register(request);
        if (!"注册成功".equals(result)) {
            return ApiResponse.fail(result);
        }

        return ApiResponse.success("注册成功");
    }

    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request,
                                                HttpServletRequest httpServletRequest) {
        log.info("收到用户登录请求，account={}, rememberMe={}", request.getAccount(), request.getRememberMe());

        UserLoginResponse response = userService.login(request, httpServletRequest);
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        log.info("收到用户登出请求");
        String result = userService.logout(request);
        return ApiResponse.success(result);
    }
    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        log.info("收到获取验证码请求");
        CaptchaResponse response = userService.generateCaptcha();
        return ApiResponse.success("获取验证码成功", response);
    }
    @PostMapping("/register/send-code")
    public ApiResponse<SendCodeResponse> sendRegisterCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送注册验证码请求，email={}", request.getEmail());
        SendCodeResponse response = userService.sendRegisterCode(request);
        return ApiResponse.success("发送注册验证码成功", response);
    }
    @PostMapping("/password/forgot")
    public ApiResponse<SendCodeResponse> forgotPassword(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送找回密码验证码请求，email={}", request.getEmail());
        SendCodeResponse response = userService.sendPasswordResetCode(request);
        return ApiResponse.success("发送找回密码验证码成功", response);
    }
    @PostMapping("/password/reset")
    public ApiResponse<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("收到重置密码请求，email={}", request.getEmail());
        String result = userService.resetPassword(request);
        return ApiResponse.success(result);
    }
}