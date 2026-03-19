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
 * 功能：处理用户相关的请求，包括获取用户信息、更新个人资料、修改密码等
 * 说明：使用 @RestController 注解标记为 REST 控制器，@RequestMapping 指定基础路径
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
     * 功能：查询所有用户列表
     * @return Map<String, Object> 用户列表结果
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
     * 功能：获取当前登录用户的信息
     * @param request HTTP 请求对象
     * @return ApiResponse<UserMeResponse> 当前用户信息
     */
    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(HttpServletRequest request) {
        log.info("收到获取当前用户信息请求");
        UserMeResponse response = userService.getCurrentUser(request);
        return ApiResponse.success("获取当前用户成功", response);
    }

    /**
     * 更新个人资料
     * <p>
     * 功能：更新当前用户的个人资料
     * @param request 更新个人资料请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return ApiResponse<String> 更新结果
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
     * @param request 修改密码请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return ApiResponse<String> 修改结果
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
     * 功能：根据用户ID获取用户信息
     * @param id 用户ID
     * @return ApiResponse<UserMeResponse> 用户信息
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
     * 功能：获取当前用户的偏好设置
     * @param request HTTP 请求对象
     * @return ApiResponse<UserPreferencesResponse> 用户偏好设置
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
     * 功能：更新当前用户的偏好设置
     * @param request 更新偏好设置请求参数
     * @param httpServletRequest HTTP 请求对象
     * @return ApiResponse<String> 更新结果
     */
    @PutMapping("/me/preferences")
    public ApiResponse<String> updateMyPreferences(@RequestBody UserPreferencesUpdateRequest request,
                                                   HttpServletRequest httpServletRequest) {
        log.info("收到更新用户偏好请求");
        String result = userService.updateMyPreferences(request, httpServletRequest);
        return ApiResponse.success(result);
    }
}