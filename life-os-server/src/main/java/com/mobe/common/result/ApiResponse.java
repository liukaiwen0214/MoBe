package com.mobe.common.result;

import lombok.Data;

/**
 * API响应类
 * <p>
 * 功能：统一的API响应格式，包含成功状态、消息和数据
 * 说明：使用泛型 T 支持不同类型的数据返回
 */
@Data
public class ApiResponse<T> {

    /**
     * 成功状态
     * <p>
     * 说明：true 表示成功，false 表示失败
     */
    private boolean success;
    
    /**
     * 响应消息
     * <p>
     * 说明：返回的消息内容
     */
    private String message;
    
    /**
     * 响应数据
     * <p>
     * 说明：返回的数据，类型为 T
     */
    private T data;

    /**
     * 创建成功响应
     * <p>
     * 功能：创建一个成功的响应对象，只包含消息
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    /**
     * 创建成功响应
     * <p>
     * 功能：创建一个成功的响应对象，包含消息和数据
     * @param message 响应消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse<T> 成功响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    /**
     * 创建失败响应
     * <p>
     * 功能：创建一个失败的响应对象，只包含消息
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ApiResponse<T> 失败响应对象
     */
    public static <T> ApiResponse<T> fail(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}