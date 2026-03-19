package com.mobe.common.exception;

/**
 * 业务异常类
 * <p>
 * 功能：用于处理业务逻辑中的异常情况
 * 说明：继承自 RuntimeException，用于在业务逻辑中抛出异常
 */
public class BizException extends RuntimeException {

    /**
     * 构造方法
     * <p>
     * 功能：创建业务异常实例
     * @param message 异常消息
     */
    public BizException(String message) {
        super(message);
    }
}