package com.mobe.common.result;

import lombok.Data;

/**
 * API响应类
 * <p>
 * 文件用途：定义统一的API响应格式，确保所有API返回结构一致
 * 所属模块：common（公共模块）
 * 核心职责：封装API响应的成功状态、消息和数据，提供静态方法快速创建响应对象
 * 与其他模块的关联：被所有控制器模块使用，作为API返回的标准格式
 * 在整体业务流程中的位置：位于响应处理层，为所有API调用提供统一的响应结构
 * 说明：使用泛型 T 支持不同类型的数据返回，使用 @Data 注解自动生成 getter、setter、toString 等方法
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