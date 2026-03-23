package com.mobe.user.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.user.dto.*;
import com.mobe.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 功能：处理用户认证相关的请求，包括注册、登录、登出、验证码等
 * 说明：使用 @RestController 注解标记为 REST 控制器，@RequestMapping 指定基础路径
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

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
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户注册
     * <p>
     * 功能：处理用户注册请求
     * @param request 注册请求参数
     * @return ApiResponse<String> 注册结果
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("收到用户注册请求，username={}, email={}", request.getUsername(), request.getEmail());

        String result = userService.register(request);
        if (!"注册成功".equals(result)) {
            return ApiResponse.fail(result);
        }

        return ApiResponse.success("注册成功");
    }

    /**
     * 用户登录
     * <p>
     * 功能：处理用户登录请求
     * @param request 登录请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return ApiResponse<UserLoginResponse> 登录结果，包含用户信息和令牌
     */
    @PostMapping("/login")
    public ApiResponse<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request,
                                                HttpServletRequest httpServletRequest) {
        log.info("收到用户登录请求，account={}, code={}, rememberMe={}", request.getAccount(),request.getCode(), request.getRememberMe());

        UserLoginResponse response = userService.login(request, httpServletRequest);
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 用户登出
     * <p>
     * 功能：处理用户登出请求
     * @param request HTTP 请求对象
     * @return ApiResponse<String> 登出结果
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpServletRequest request) {
        log.info("收到用户登出请求");
        String result = userService.logout(request);
        return ApiResponse.success(result);
    }
    
    /**
     * 获取验证码
     * <p>
     * 功能：生成并返回验证码
     * @return ApiResponse<CaptchaResponse> 验证码结果
     */
    @GetMapping("/captcha")
    public ApiResponse<CaptchaResponse> captcha() {
        log.info("收到获取验证码请求");
        CaptchaResponse response = userService.generateCaptcha();
        return ApiResponse.success("获取验证码成功", response);
    }
    
    /**
     * 发送注册验证码
     * <p>
     * 功能：发送注册验证码到用户邮箱
     * @param request 发送验证码请求参数
     * @return ApiResponse<SendCodeResponse> 发送结果
     */
    @PostMapping("/register/send-code")
    public ApiResponse<SendCodeResponse> sendRegisterCode(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送注册验证码请求，email={}", request.getEmail());
        SendCodeResponse response = userService.sendRegisterCode(request);
        return ApiResponse.success("发送注册验证码成功", response);
    }
    
    /**
     * 发送找回密码验证码
     * <p>
     * 功能：发送找回密码验证码到用户邮箱
     * @param request 发送验证码请求参数
     * @return ApiResponse<SendCodeResponse> 发送结果
     */
    @PostMapping("/password/forgot")
    public ApiResponse<SendCodeResponse> forgotPassword(@Valid @RequestBody SendCodeRequest request) {
        log.info("收到发送找回密码验证码请求，email={}", request.getEmail());
        SendCodeResponse response = userService.sendPasswordResetCode(request);
        return ApiResponse.success("发送找回密码验证码成功", response);
    }
    
    /**
     * 重置密码
     * <p>
     * 功能：根据验证码重置用户密码
     * @param request 重置密码请求参数
     * @return ApiResponse<String> 重置结果
     */
    @PostMapping("/password/reset")
    public ApiResponse<String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        log.info("收到重置密码请求，email={}", request.getEmail());
        String result = userService.resetPassword(request);
        return ApiResponse.success(result);
    }
}