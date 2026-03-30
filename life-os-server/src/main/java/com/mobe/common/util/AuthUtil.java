package com.mobe.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证工具类
 * <p>
 * 文件用途：提供认证相关的工具方法，用于处理用户认证信息
 * 所属模块：common（公共模块）
 * 核心职责：从HTTP请求中提取和处理认证相关信息，如Bearer Token
 * 与其他模块的关联：被用户模块和拦截器模块使用，用于用户身份验证
 * 在整体业务流程中的位置：位于认证处理层，为用户认证提供工具支持
 * 说明：包含获取 Bearer Token 等认证相关的工具方法
 */
public class AuthUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private AuthUtil() {
    }

    /**
     * 从 HTTP 请求中获取 Bearer Token
     * <p>
     * 功能：从 Authorization 头中提取 Bearer Token
     * @param request HTTP 请求对象
     * @return String Bearer Token，如果不存在或格式不正确则返回 null
     */
    public static String getBearerToken(HttpServletRequest request) {
        // 获取 Authorization 头
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        // 检查是否以 "Bearer " 开头
        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        // 提取并返回 Token
        return authorization.substring(7).trim();
    }
}