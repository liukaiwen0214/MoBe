package com.mobe.checklist.controller;

import com.mobe.checklist.dto.request.ChecklistCreateRequest;
import com.mobe.checklist.dto.request.ChecklistPageRequest;
import com.mobe.checklist.dto.request.ChecklistUpdateRequest;
import com.mobe.checklist.dto.response.ChecklistItemResponse;
import com.mobe.checklist.service.ChecklistService;
import com.mobe.common.result.ApiResponse;
import com.mobe.finance.dto.response.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 清单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/checklists")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    /**
     * 新增清单
     */
    @PostMapping("/create")
    public ApiResponse<Void> createChecklist(@Valid @RequestBody ChecklistCreateRequest request,
                                             HttpServletRequest httpServletRequest) {
        log.info("收到新增清单请求");
        checklistService.createChecklist(request, httpServletRequest);
        return ApiResponse.success("新增成功");
    }

    /**
     * 分页查询清单
     */
    @PostMapping("/page")
    public ApiResponse<PageResponse<ChecklistItemResponse>> pageChecklists(@RequestBody ChecklistPageRequest request,
                                                                           HttpServletRequest httpServletRequest) {
        log.info("收到分页查询清单请求");
        PageResponse<ChecklistItemResponse> response = checklistService.pageChecklists(request, httpServletRequest);
        return ApiResponse.success("查询成功", response);
    }

    /**
     * 更新清单
     */
    @PostMapping("/update")
    public ApiResponse<Void> updateChecklist(@Valid @RequestBody ChecklistUpdateRequest request,
                                             HttpServletRequest httpServletRequest) {
        log.info("收到更新清单请求");
        checklistService.updateChecklist(request, httpServletRequest);
        return ApiResponse.success("更新成功");
    }

    /**
     * 完成清单
     */
    @PostMapping("/complete/{id}")
    public ApiResponse<Void> completeChecklist(@PathVariable String id,
                                               HttpServletRequest httpServletRequest) {
        log.info("收到完成清单请求，id={}", id);
        checklistService.completeChecklist(id, httpServletRequest);
        return ApiResponse.success("操作成功");
    }

    /**
     * 恢复为未完成
     */
    @PostMapping("/restore/{id}")
    public ApiResponse<Void> restoreChecklist(@PathVariable String id,
                                              HttpServletRequest httpServletRequest) {
        log.info("收到恢复清单请求，id={}", id);
        checklistService.restoreChecklist(id, httpServletRequest);
        return ApiResponse.success("操作成功");
    }

    /**
     * 删除清单
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> deleteChecklist(@PathVariable String id,
                                             HttpServletRequest httpServletRequest) {
        log.info("收到删除清单请求，id={}", id);
        checklistService.deleteChecklist(id, httpServletRequest);
        return ApiResponse.success("删除成功");
    }
}