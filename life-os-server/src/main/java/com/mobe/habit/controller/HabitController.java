package com.mobe.habit.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.habit.dto.request.HabitCreateRequest;
import com.mobe.habit.dto.request.HabitListRequest;
import com.mobe.habit.dto.response.HabitSimpleResponse;
import com.mobe.habit.service.HabitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

    @Resource
    private HabitService habitService;

    /**
     * 查询习惯列表（简单）
     */
    @PostMapping("/simple-list")
    public ApiResponse<List<HabitSimpleResponse>> listSimpleHabits(@RequestBody HabitListRequest request,
                                                                   HttpServletRequest httpServletRequest) {
        log.info("收到查询习惯简单列表请求");
        List<HabitSimpleResponse> response = habitService.listSimpleHabits(request, httpServletRequest);
        return ApiResponse.success("查询成功", response);
    }

    /**
     * 新增习惯
     */
    @PostMapping("/create")
    public ApiResponse<Boolean> createHabit(@RequestBody HabitCreateRequest request,
                                            HttpServletRequest httpServletRequest) {
        log.info("收到新增习惯请求");
        habitService.createHabit(request, httpServletRequest);
        return ApiResponse.success("新增成功", true);
    }
}