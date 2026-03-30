package com.mobe.user.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.user.dto.UserMeResponse;
import com.mobe.user.dto.UserPasswordUpdateRequest;
import com.mobe.user.dto.UserProfileUpdateRequest;
import com.mobe.user.entity.UserEntity;
import com.mobe.user.mapper.UserMapper;
import com.mobe.user.service.UserService;
import com.mobe.user.dto.UserPreferencesResponse;
import com.mobe.user.dto.UserPreferencesUpdateRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * <p>
 * 文件用途：处理用户相关的HTTP请求，提供用户信息管理、个人资料更新、密码修改等功能
 * 所属模块：user（用户模块）
 * 核心职责：处理用户相关的API请求，调用用户服务完成业务逻辑
 * 与其他模块的关联：依赖于userService提供的业务逻辑，使用common模块的ApiResponse作为响应格式
 * 在整体业务流程中的位置：位于控制器层，是用户相关API的入口点
 * 说明：使用 @RestController 注解标记为 REST 控制器，@RequestMapping 指定基础路径为/api/v1/users
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    /**
     * 用户Mapper
     */
    private final UserMapper userMapper;
    
    /**
     * 用户服务
     */
    private final UserService userService;

    /**
     * 构造方法
     * <p>
     * 功能：注入用户Mapper和用户服务
     * @param userMapper 用户Mapper实例
     * @param userService 用户服务实例
     */
    public UserController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * 测试接口
     * <p>
     * 功能：查询所有用户列表，用于测试目的
     * @return Map<String, Object> 包含成功状态、用户数量和用户列表的结果
     * 调用时机：手动调用/test接口时
     * 核心流程：1. 记录开始日志 2. 查询所有用户 3. 记录完成日志 4. 构建并返回结果
     * 边界情况：无特殊边界情况
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        log.info("开始查询用户列表");

        List<UserEntity> users = userMapper.selectList(null);

        log.info("查询用户列表完成，数量={}", users.size());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", users.size());
        result.put("data", users);
        return result;
    }

    /**
     * 获取当前用户信息
     * <p>
     * 功能：获取当前登录用户的详细信息
     * @param request HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<UserMeResponse> 包含当前用户信息的响应
     * 调用时机：用户访问/me接口时
     * 核心流程：1. 记录请求日志 2. 调用userService获取当前用户信息 3. 返回成功响应
     * 边界情况：用户未登录时，userService会处理并返回相应错误
     */
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(HttpServletRequest request) {
        log.info("收到获取当前用户信息请求，请求参数={}", request);
        UserMeResponse response = userService.getCurrentUser(request);
        return ApiResponse.success("获取当前用户成功", response);
    }

    /**
     * 更新个人资料
     * <p>
     * 功能：更新当前用户的个人资料信息
     * @param request 更新个人资料请求参数，包含要更新的个人信息
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含更新结果的响应
     * 调用时机：用户提交个人资料更新请求时
     * 核心流程：1. 记录请求日志 2. 调用userService更新个人资料 3. 返回成功响应
     * 边界情况：用户未登录时，userService会处理并返回相应错误；请求参数不符合要求时会触发参数校验异常
     */
    @PutMapping("/me/profile")
    public ApiResponse<String> updateProfile(@RequestBody UserProfileUpdateRequest request,
                                             HttpServletRequest httpServletRequest) {
        log.info("收到更新个人资料请求");
        String result = userService.updateProfile(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 修改密码
     * <p>
     * 功能：修改当前用户的密码
     * @param request 修改密码请求参数，包含旧密码和新密码
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含修改结果的响应
     * 调用时机：用户提交密码修改请求时
     * 核心流程：1. 记录请求日志 2. 调用userService修改密码 3. 返回成功响应
     * 边界情况：用户未登录时，userService会处理并返回相应错误；旧密码错误时会返回相应错误；请求参数不符合要求时会触发参数校验异常
     */
    @PutMapping("/me/password")
    public ApiResponse<String> updatePassword(@Valid @RequestBody UserPasswordUpdateRequest request,
                                              HttpServletRequest httpServletRequest) {
        log.info("收到修改密码请求");
        String result = userService.updatePassword(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 根据ID获取用户信息
     * <p>
     * 功能：根据用户ID获取指定用户的详细信息
     * @param id 用户ID
     * @return ApiResponse<UserMeResponse> 包含用户信息的响应
     * 调用时机：访问/{id}接口时
     * 核心流程：1. 记录请求日志 2. 调用userService根据ID获取用户信息 3. 返回成功响应
     * 边界情况：用户不存在时，userService会处理并返回相应错误
     */
    @GetMapping("/{id}")
    public ApiResponse<UserMeResponse> getUserById(@PathVariable String id) {
        log.info("收到根据ID获取用户信息请求，id={}", id);
        UserMeResponse response = userService.getUserById(id);
        return ApiResponse.success("获取用户信息成功", response);
    }
    
    /**
     * 获取用户偏好设置
     * <p>
     * 功能：获取当前用户的偏好设置信息
     * @param request HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<UserPreferencesResponse> 包含用户偏好设置的响应
     * 调用时机：用户访问/me/preferences接口时
     * 核心流程：1. 记录请求日志 2. 调用userService获取用户偏好设置 3. 返回成功响应
     * 边界情况：用户未登录时，userService会处理并返回相应错误
     */
    @GetMapping("/me/preferences")
    public ApiResponse<UserPreferencesResponse> getMyPreferences(HttpServletRequest request) {
        log.info("收到获取用户偏好请求");
        UserPreferencesResponse response = userService.getMyPreferences(request);
        return ApiResponse.success("获取用户偏好成功", response);
    }

    /**
     * 更新用户偏好设置
     * <p>
     * 功能：更新当前用户的偏好设置信息
     * @param request 更新偏好设置请求参数，包含要更新的偏好设置
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含更新结果的响应
     * 调用时机：用户提交偏好设置更新请求时
     * 核心流程：1. 记录请求日志 2. 调用userService更新用户偏好设置 3. 返回成功响应
     * 边界情况：用户未登录时，userService会处理并返回相应错误；请求参数不符合要求时会触发参数校验异常
     */
    @PutMapping("/me/preferences")
    public ApiResponse<String> updateMyPreferences(@RequestBody UserPreferencesUpdateRequest request,
                                                   HttpServletRequest httpServletRequest) {
        log.info("收到更新用户偏好请求");
        String result = userService.updateMyPreferences(request, httpServletRequest);
        return ApiResponse.success(result);
    }
}