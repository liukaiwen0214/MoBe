package com.mobe.habit.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.habit.dto.request.HabitCreateDetailRequest;
import com.mobe.habit.dto.request.HabitCreateRequest;
import com.mobe.habit.dto.request.HabitListRequest;
import com.mobe.habit.dto.response.HabitSimpleResponse;
import com.mobe.habit.service.HabitService;
import com.mobe.habit.dto.request.HabitPageRequest;
import com.mobe.habit.dto.request.HabitRecordPageRequest;
import com.mobe.habit.dto.request.HabitToggleGenerateRequest;
import com.mobe.habit.dto.request.HabitUpdateRequest;
import com.mobe.habit.dto.response.HabitPageItemResponse;
import com.mobe.habit.dto.response.HabitRecordItemResponse;
import com.mobe.common.result.PageResult;

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

    @PostMapping("/page")
    public ApiResponse<PageResult<HabitPageItemResponse>> pageHabits(@RequestBody HabitPageRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到分页查询习惯请求，请求参数={}", request);
        PageResult<HabitPageItemResponse> response = habitService.pageHabits(request, httpServletRequest);
        return ApiResponse.success("查询成功", response);
    }

    @PostMapping("/create-detail")
    public ApiResponse<String> createHabitDetail(@RequestBody HabitCreateDetailRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到新增习惯请求，请求参数={}", request);
        String result = habitService.createHabitDetail(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    @PostMapping("/update")
    public ApiResponse<String> updateHabit(@RequestBody HabitUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到更新习惯请求，请求参数={}", request);
        String result = habitService.updateHabit(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<String> deleteHabit(@PathVariable String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到删除习惯请求，id={}", id);
        String result = habitService.deleteHabit(id, httpServletRequest);
        return ApiResponse.success(result);
    }

    @PostMapping("/enable/{id}")
    public ApiResponse<String> enableHabit(@PathVariable String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到启用习惯请求，id={}", id);
        String result = habitService.enableHabit(id, httpServletRequest);
        return ApiResponse.success(result);
    }

    @PostMapping("/disable/{id}")
    public ApiResponse<String> disableHabit(@PathVariable String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到停用习惯请求，id={}", id);
        String result = habitService.disableHabit(id, httpServletRequest);
        return ApiResponse.success(result);
    }

    @PostMapping("/toggle-generate")
    public ApiResponse<String> toggleHabitGenerate(@RequestBody HabitToggleGenerateRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到切换习惯生成到清单请求，请求参数={}", request);
        String result = habitService.toggleHabitGenerate(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    @PostMapping("/records/page")
    public ApiResponse<PageResult<HabitRecordItemResponse>> pageHabitRecords(
            @RequestBody HabitRecordPageRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到分页查询习惯记录请求，请求参数={}", request);
        PageResult<HabitRecordItemResponse> result = habitService.pageHabitRecords(request, httpServletRequest);
        return ApiResponse.success("查询成功", result);
    }
}