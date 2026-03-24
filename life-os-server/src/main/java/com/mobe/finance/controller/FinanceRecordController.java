package com.mobe.finance.controller;

import com.mobe.common.result.ApiResponse;
import com.mobe.finance.dto.request.FinanceRecordBatchCreateRequest;
import com.mobe.finance.dto.request.FinanceRecordCreateRequest;
import com.mobe.finance.dto.request.FinanceRecordPageRequest;
import com.mobe.finance.dto.request.FinanceRecordUpdateRequest;
import com.mobe.finance.dto.response.FinanceRecordResponse;
import com.mobe.finance.dto.response.PageResponse;
import com.mobe.finance.service.FinanceRecordService;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 财务记录控制器
 * <p>
 * 功能：处理财务记录相关的请求，包括创建、查询、删除财务记录
 * 说明：使用 @RestController 注解标记为 REST 控制器，@RequestMapping 指定基础路径
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/bills/")
public class FinanceRecordController {

    /**
     * 财务记录服务
     */
    private final FinanceRecordService financeRecordService;

    /**
     * 构造方法
     * <p>
     * 功能：注入财务记录服务
     * 
     * @param financeRecordService 财务记录服务实例
     */
    public FinanceRecordController(FinanceRecordService financeRecordService) {
        this.financeRecordService = financeRecordService;
    }

    /**
     * 创建财务记录
     * <p>
     * 功能：创建新的财务记录
     * 
     * @param request 创建财务记录请求参数
     * @param session HTTP会话对象
     * @return ApiResponse<Void> 创建结果
     */
    @PostMapping("/create")
    public ApiResponse<Void> createRecord(@Valid @RequestBody FinanceRecordCreateRequest request,
            HttpServletRequest httpServletRequest) {
        financeRecordService.createRecord(request, httpServletRequest);
        return ApiResponse.success("新增流水成功");
    }

    /**
     * 分页查询财务记录
     * <p>
     * 功能：根据条件分页查询财务记录
     * 
     * @param request 分页查询请求参数
     * @param session HTTP会话对象
     * @return ApiResponse<PageResponse<FinanceRecordResponse>> 查询结果
     */
    @PostMapping("/page")
    public ApiResponse<PageResponse<FinanceRecordResponse>> pageRecords(@RequestBody FinanceRecordPageRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success("查询成功", financeRecordService.pageRecords(request, httpServletRequest));
    }

    /**
     * 删除财务记录
     * <p>
     * 功能：删除指定的财务记录
     * 
     * @param id      财务记录ID
     * @param session HTTP会话对象
     * @return ApiResponse<Void> 删除结果
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable String id,
            HttpServletRequest httpServletRequest) {
        log.info("收到删除财务记录请求，ID={}", id);
        financeRecordService.deleteRecord(id, httpServletRequest);
        return ApiResponse.success("删除成功");
    }

    /**
     * 更新财务记录
     * <p>
     * 功能：更新指定的财务记录
     * 
     * @param id      财务记录ID
     * @param request 更新财务记录请求参数
     * @param session HTTP会话对象
     * @return ApiResponse<Void> 更新结果
     */
    @PostMapping("/update/{id}")
    public ApiResponse<Void> updateRecord(@PathVariable String id,
            @Valid @RequestBody FinanceRecordUpdateRequest request, HttpServletRequest httpServletRequest) {
        financeRecordService.updateRecord(id, request, httpServletRequest);
        return ApiResponse.success("修改成功");
    }
    /**
     * 批量创建财务记录
     * <p>
     * 功能：批量创建新的财务记录
     * 
     * @param request 批量创建财务记录请求参数
     * @param session HTTP会话对象
     * @return ApiResponse<Void> 批量创建结果
     */
    @PostMapping("/batch-create")
    public ApiResponse<Void> batchCreateRecords(@Valid @RequestBody FinanceRecordBatchCreateRequest request,
            HttpServletRequest httpServletRequest) {
                log.info("收到批量创建财务记录请求，数据={}", request);
        financeRecordService.batchCreateRecords(request, httpServletRequest);
        return ApiResponse.success("批量导入成功");
    }
}