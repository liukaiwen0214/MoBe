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

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {


    private final UserMapper userMapper;
    private final UserService userService;

    public UserController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

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

    @GetMapping("/me")
    public ApiResponse<UserMeResponse> me(HttpServletRequest request) {
        log.info("收到获取当前用户信息请求");
        UserMeResponse response = userService.getCurrentUser(request);
        return ApiResponse.success("获取当前用户成功", response);
    }

    @PutMapping("/me/profile")
    public ApiResponse<String> updateProfile(@RequestBody UserProfileUpdateRequest request,
                                             HttpServletRequest httpServletRequest) {
        log.info("收到更新个人资料请求");
        String result = userService.updateProfile(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    @PutMapping("/me/password")
    public ApiResponse<String> updatePassword(@Valid @RequestBody UserPasswordUpdateRequest request,
                                              HttpServletRequest httpServletRequest) {
        log.info("收到修改密码请求");
        String result = userService.updatePassword(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserMeResponse> getUserById(@PathVariable String id) {
        log.info("收到根据ID获取用户信息请求，id={}", id);
        UserMeResponse response = userService.getUserById(id);
        return ApiResponse.success("获取用户信息成功", response);
    }
    @GetMapping("/me/preferences")
    public ApiResponse<UserPreferencesResponse> getMyPreferences(HttpServletRequest request) {
        log.info("收到获取用户偏好请求");
        UserPreferencesResponse response = userService.getMyPreferences(request);
        return ApiResponse.success("获取用户偏好成功", response);
    }

    @PutMapping("/me/preferences")
    public ApiResponse<String> updateMyPreferences(@RequestBody UserPreferencesUpdateRequest request,
                                                   HttpServletRequest httpServletRequest) {
        log.info("收到更新用户偏好请求");
        String result = userService.updateMyPreferences(request, httpServletRequest);
        return ApiResponse.success(result);
    }
}