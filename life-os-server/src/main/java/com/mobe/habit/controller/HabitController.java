package com.mobe.habit.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.habit.dto.request.HabitCreateDetailRequest;
import com.mobe.habit.dto.request.HabitCreateRequest;
import com.mobe.habit.dto.request.HabitListRequest;
import com.mobe.habit.dto.response.HabitSimpleResponse;
import com.mobe.habit.dto.response.HabitStatsResponse;
import com.mobe.habit.dto.response.HabitTimelineItemResponse;
import com.mobe.habit.service.HabitService;
import com.mobe.habit.dto.request.HabitPageRequest;
import com.mobe.habit.dto.request.HabitRecordPageRequest;
import com.mobe.habit.dto.request.HabitTimelinePageRequest;
import com.mobe.habit.dto.request.HabitToggleGenerateRequest;
import com.mobe.habit.dto.request.HabitUpdateRequest;
import com.mobe.habit.dto.response.HabitPageItemResponse;
import com.mobe.habit.dto.response.HabitRecordItemResponse;
import com.mobe.common.result.PageResult;
import com.mobe.finance.dto.response.PageResponse;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 习惯控制器
 * <p>
 * 文件用途：处理习惯相关的HTTP请求，提供习惯的创建、查询、更新、删除等功能
 * 所属模块：habit（习惯模块）
 * 核心职责：处理习惯相关的API请求，调用习惯服务完成业务逻辑
 * 与其他模块的关联：依赖于habitService提供的业务逻辑，使用common模块的ApiResponse和PageResult作为响应格式
 * 在整体业务流程中的位置：位于控制器层，是习惯相关API的入口点
 * 说明：使用 @RestController 注解标记为 REST 控制器，@RequestMapping 指定基础路径为/api/v1/habits
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/habits")
public class HabitController {

    @Resource
    private HabitService habitService;

    /**
     * 查询习惯列表（简单）
     * <p>
     * 功能：查询用户的习惯列表，返回简化的习惯信息
     * @param request 查询参数，包含查询条件
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<List<HabitSimpleResponse>> 包含习惯列表的响应
     * 调用时机：用户访问/simple-list接口时
     * 核心流程：1. 记录请求日志 2. 调用habitService查询简单习惯列表 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误
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
     * <p>
     * 功能：创建新的习惯
     * @param request 创建习惯的请求参数，包含习惯名称、描述等信息
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<Boolean> 包含创建结果的响应
     * 调用时机：用户提交创建习惯请求时
     * 核心流程：1. 记录请求日志 2. 调用habitService创建习惯 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；请求参数不符合要求时会触发参数校验异常
     */
    @PostMapping("/create")
    public ApiResponse<Boolean> createHabit(@RequestBody HabitCreateRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到新增习惯请求");
        habitService.createHabit(request, httpServletRequest);
        return ApiResponse.success("新增成功", true);
    }

    /**
     * 分页查询习惯列表
     * <p>
     * 功能：分页查询用户的习惯列表，返回详细的习惯信息
     * @param request 分页查询参数，包含页码、每页大小、查询条件等
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<PageResult<HabitPageItemResponse>> 包含分页习惯列表的响应
     * 调用时机：用户访问/page接口时
     * 核心流程：1. 记录请求日志 2. 调用habitService分页查询习惯 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误
     */
    @PostMapping("/page")
    public ApiResponse<PageResult<HabitPageItemResponse>> pageHabits(@RequestBody HabitPageRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到分页查询习惯请求，请求参数={}", request);
        PageResult<HabitPageItemResponse> response = habitService.pageHabits(request, httpServletRequest);
        return ApiResponse.success("查询成功", response);
    }

    /**
     * 新增习惯（详细）
     * <p>
     * 功能：创建新的习惯，包含详细的习惯信息
     * @param request 创建习惯的详细请求参数，包含习惯的各种详细信息
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含创建结果的响应
     * 调用时机：用户提交创建详细习惯请求时
     * 核心流程：1. 记录请求日志 2. 调用habitService创建详细习惯 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；请求参数不符合要求时会触发参数校验异常
     */
    @PostMapping("/create-detail")
    public ApiResponse<String> createHabitDetail(@RequestBody HabitCreateDetailRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到新增习惯请求，请求参数={}", request);
        String result = habitService.createHabitDetail(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 更新习惯
     * <p>
     * 功能：更新已有的习惯信息
     * @param request 更新习惯的请求参数，包含习惯ID和要更新的信息
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含更新结果的响应
     * 调用时机：用户提交更新习惯请求时
     * 核心流程：1. 记录请求日志 2. 调用habitService更新习惯 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；请求参数不符合要求时会触发参数校验异常；习惯不存在时会返回相应错误
     */
    @PostMapping("/update")
    public ApiResponse<String> updateHabit(@RequestBody HabitUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到更新习惯请求，请求参数={}", request);
        String result = habitService.updateHabit(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 删除习惯
     * <p>
     * 功能：删除指定的习惯
     * @param id 习惯ID
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含删除结果的响应
     * 调用时机：用户提交删除习惯请求时
     * 核心流程：1. 记录请求日志 2. 调用habitService删除习惯 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；习惯不存在时会返回相应错误；用户无权限删除该习惯时会返回相应错误
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<String> deleteHabit(@PathVariable String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到删除习惯请求，id={}", id);
        String result = habitService.deleteHabit(id, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 启用习惯
     * <p>
     * 功能：启用指定的习惯
     * @param id 习惯ID
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含启用结果的响应
     * 调用时机：用户提交启用习惯请求时
     * 核心流程：1. 记录请求日志 2. 调用habitService启用习惯 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；习惯不存在时会返回相应错误；用户无权限操作该习惯时会返回相应错误
     */
    @PostMapping("/enable/{id}")
    public ApiResponse<String> enableHabit(@PathVariable String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到启用习惯请求，id={}", id);
        String result = habitService.enableHabit(id, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 停用习惯
     * <p>
     * 功能：停用指定的习惯
     * @param id 习惯ID
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含停用结果的响应
     * 调用时机：用户提交停用习惯请求时
     * 核心流程：1. 记录请求日志 2. 调用habitService停用习惯 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；习惯不存在时会返回相应错误；用户无权限操作该习惯时会返回相应错误
     */
    @PostMapping("/disable/{id}")
    public ApiResponse<String> disableHabit(@PathVariable String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到停用习惯请求，id={}", id);
        String result = habitService.disableHabit(id, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 切换习惯生成到清单
     * <p>
     * 功能：切换习惯是否生成到清单
     * @param request 切换生成状态的请求参数，包含习惯ID和生成状态
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<String> 包含切换结果的响应
     * 调用时机：用户提交切换习惯生成到清单请求时
     * 核心流程：1. 记录请求日志 2. 调用habitService切换生成状态 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；习惯不存在时会返回相应错误；用户无权限操作该习惯时会返回相应错误
     */
    @PostMapping("/toggle-generate")
    public ApiResponse<String> toggleHabitGenerate(@RequestBody HabitToggleGenerateRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到切换习惯生成到清单请求，请求参数={}", request);
        String result = habitService.toggleHabitGenerate(request, httpServletRequest);
        return ApiResponse.success(result);
    }

    /**
     * 分页查询习惯记录
     * <p>
     * 功能：分页查询用户的习惯记录
     * @param request 分页查询参数，包含页码、每页大小、查询条件等
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<PageResult<HabitRecordItemResponse>> 包含分页习惯记录的响应
     * 调用时机：用户访问/records/page接口时
     * 核心流程：1. 记录请求日志 2. 调用habitService分页查询习惯记录 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误
     */
    @PostMapping("/records/page")
    public ApiResponse<PageResult<HabitRecordItemResponse>> pageHabitRecords(
            @RequestBody HabitRecordPageRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到分页查询习惯记录请求，请求参数={}", request);
        PageResult<HabitRecordItemResponse> result = habitService.pageHabitRecords(request, httpServletRequest);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 查询习惯统计信息
     * <p>
     * 功能：查询指定习惯的统计信息
     * @param id 习惯ID
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<HabitStatsResponse> 包含习惯统计信息的响应
     * 调用时机：用户访问/stats/{id}接口时
     * 核心流程：1. 记录请求日志 2. 调用habitService获取习惯统计信息 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误；习惯不存在时会返回相应错误；用户无权限查看该习惯时会返回相应错误
     */
    @PostMapping("/stats/{id}")
    public ApiResponse<HabitStatsResponse> getHabitStats(
            @PathVariable("id") String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到查询习惯统计请求，id={}", id);
        HabitStatsResponse result = habitService.getHabitStats(id, httpServletRequest);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 分页查询习惯时间轴
     * <p>
     * 功能：分页查询用户的习惯时间轴
     * @param request 分页查询参数，包含页码、每页大小、查询条件等
     * @param httpServletRequest HTTP 请求对象，用于获取用户认证信息
     * @return ApiResponse<PageResponse<HabitTimelineItemResponse>> 包含分页习惯时间轴的响应
     * 调用时机：用户访问/timeline/page接口时
     * 核心流程：1. 记录请求日志 2. 调用habitService分页查询习惯时间轴 3. 返回成功响应
     * 边界情况：用户未登录时，habitService会处理并返回相应错误
     */
    @PostMapping("/timeline/page")
    public ApiResponse<PageResponse<HabitTimelineItemResponse>> pageHabitTimeline(
            @RequestBody HabitTimelinePageRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("收到分页查询习惯时间轴请求");
        PageResponse<HabitTimelineItemResponse> result = habitService.pageHabitTimeline(request, httpServletRequest);
        return ApiResponse.success("查询成功", result);
    }
}