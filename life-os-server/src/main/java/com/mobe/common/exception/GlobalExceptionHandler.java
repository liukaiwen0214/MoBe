package com.mobe.common.exception;

import com.mobe.common.result.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 文件用途：统一处理系统中所有的异常，返回标准的API响应格式
 * 所属模块：common（公共模块）
 * 核心职责：捕获并处理各种类型的异常，确保API返回格式一致
 * 与其他模块的关联：被所有模块使用，处理模块中抛出的各种异常
 * 在整体业务流程中的位置：位于异常处理层，为所有API调用提供统一的异常处理机制
 * 说明：使用 @RestControllerAdvice 注解标记为全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理方法参数校验异常
     * <p>
     * 功能：处理 @Valid 注解验证失败的异常，通常是请求参数不符合验证规则
     * @param ex 方法参数校验异常对象
     * @return ResponseEntity<ApiResponse<String>> 包含错误信息的响应实体
     * 核心流程：1. 从异常中获取具体的错误信息 2. 记录警告日志 3. 返回400错误和错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数校验失败";

        log.warn("参数校验失败: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    /**
     * 处理参数约束异常
     * <p>
     * 功能：处理 @NotNull、@Size 等约束注解验证失败的异常，通常是请求参数不符合约束规则
     * @param ex 参数约束异常对象
     * @return ResponseEntity<ApiResponse<String>> 包含错误信息的响应实体
     * 核心流程：1. 记录警告日志 2. 返回400错误和通用错误信息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("参数约束异常: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("请求参数不合法"));
    }

    /**
     * 处理请求体反序列化异常
     * <p>
     * 功能：处理 JSON 格式错误、时间格式错误等请求体解析异常
     * @param ex 请求体反序列化异常对象
     * @return ResponseEntity<ApiResponse<String>> 包含错误信息的响应实体
     * 核心流程：1. 记录警告日志 2. 返回400错误和通用错误信息
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("请求参数格式错误"));
    }

    /**
     * 处理业务异常
     * <p>
     * 功能：处理业务逻辑中抛出的 BizException，通常是业务规则验证失败
     * @param ex 业务异常对象
     * @return ResponseEntity<ApiResponse<String>> 包含错误信息的响应实体
     * 核心流程：1. 记录警告日志 2. 返回400错误和业务异常信息
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<String>> handleBizException(BizException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(ex.getMessage()));
    }

    /**
     * 处理系统异常
     * <p>
     * 功能：处理所有未捕获的异常，作为异常处理的最后防线
     * @param ex 系统异常对象
     * @return ResponseEntity<ApiResponse<String>> 包含错误信息的响应实体
     * 核心流程：1. 记录错误日志（包含异常堆栈） 2. 返回500错误和通用错误信息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("系统异常，请稍后重试"));
    }
}