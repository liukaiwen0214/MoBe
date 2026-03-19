package com.mobe.common.exception;

import com.mobe.common.result.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 功能：处理系统中所有的异常，返回统一的API响应格式
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
     * 功能：处理 @Valid 注解验证失败的异常
     * @param ex 方法参数校验异常
     * @return ApiResponse<String> 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() != null
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数校验失败";

        log.warn("参数校验失败: {}", message);
        return ApiResponse.fail(message);
    }

    /**
     * 处理参数约束异常
     * <p>
     * 功能：处理 @NotNull、@Size 等约束注解验证失败的异常
     * @param ex 参数约束异常
     * @return ApiResponse<String> 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<String> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("参数约束异常: {}", ex.getMessage());
        return ApiResponse.fail("请求参数不合法");
    }

    /**
     * 处理系统异常
     * <p>
     * 功能：处理所有未捕获的异常
     * @param ex 系统异常
     * @return ApiResponse<String> 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ApiResponse.fail("系统异常，请稍后重试");
    }
    
    /**
     * 处理业务异常
     * <p>
     * 功能：处理业务逻辑中抛出的 BizException
     * @param ex 业务异常
     * @return ApiResponse<String> 错误响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<String> handleBizException(BizException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ApiResponse.fail(ex.getMessage());
    }
}