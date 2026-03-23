package com.mobe.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mobe.common.result.ApiResponse;
import com.mobe.finance.dto.request.FinanceRecordCreateRequest;
import com.mobe.finance.dto.request.FinanceRecordPageRequest;
import com.mobe.finance.dto.response.FinanceRecordResponse;
import com.mobe.finance.service.FinanceRecordService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 财务记录控制器
 * <p>
 * 功能：处理财务记录相关的请求，包括创建、查询、删除财务记录
 * 说明：使用 @RestController 注解标记为 REST 控制器，@RequestMapping 指定基础路径
 */
@RestController
@RequestMapping("/api/v1/finance-records")
public class FinanceRecordController {

    /**
     * 财务记录服务
     */
    private final FinanceRecordService financeRecordService;

    /**
     * 构造方法
     * <p>
     * 功能：注入财务记录服务
     * @param financeRecordService 财务记录服务实例
     */
    public FinanceRecordController(FinanceRecordService financeRecordService) {
        this.financeRecordService = financeRecordService;
    }

    /**
     * 创建财务记录
     * <p>
     * 功能：创建新的财务记录
     * @param request 创建财务记录请求参数
     * @param session HTTP会话对象
     * @return ApiResponse<Void> 创建结果
     */
    @PostMapping
    public ApiResponse<Void> createRecord(@Valid @RequestBody FinanceRecordCreateRequest request,
                                          HttpSession session) {
        financeRecordService.createRecord(request, session);
        return ApiResponse.success("新增流水成功");
    }

    /**
     * 分页查询财务记录
     * <p>
     * 功能：根据条件分页查询财务记录
     * @param request 分页查询请求参数
     * @param session HTTP会话对象
     * @return ApiResponse<IPage<FinanceRecordResponse>> 查询结果
     */
    @PostMapping("/page")
    public ApiResponse<IPage<FinanceRecordResponse>> pageRecords(@RequestBody FinanceRecordPageRequest request,
                                                                 HttpSession session) {
        return ApiResponse.success("查询成功", financeRecordService.pageRecords(request, session));
    }

    /**
     * 删除财务记录
     * <p>
     * 功能：删除指定的财务记录
     * @param id 财务记录ID
     * @param session HTTP会话对象
     * @return ApiResponse<Void> 删除结果
     */
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable String id,
                                          HttpSession session) {
        financeRecordService.deleteRecord(id, session);
        return ApiResponse.success("删除成功");
    }
}