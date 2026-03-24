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
 * 功能：处理系统中所有的异常，返回统一的 API 响应格式
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
     * 功能：处理 @NotNull、@Size 等约束注解验证失败的异常
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
     * 功能：处理 JSON 格式错误、时间格式错误等异常
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
     * 功能：处理业务逻辑中抛出的 BizException
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
     * 功能：处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("系统异常，请稍后重试"));
    }
}