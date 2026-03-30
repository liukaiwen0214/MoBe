package com.mobe.common.util;

import java.util.UUID;

/**
 * 令牌工具类
 * <p>
 * 文件用途：提供生成各种令牌的工具方法，用于生成唯一标识符
 * 所属模块：common（公共模块）
 * 核心职责：生成会话令牌等各种令牌，确保令牌的唯一性
 * 与其他模块的关联：被用户模块使用，用于生成用户会话令牌
 * 在整体业务流程中的位置：位于认证处理层，为用户认证和会话管理提供令牌支持
 * 说明：包含生成会话令牌等方法，使用UUID生成唯一令牌
 */
public class TokenUtil {

    /**
     * 私有构造方法，防止实例化
     */
    private TokenUtil() {
    }

    /**
     * 生成会话令牌
     * <p>
     * 功能：生成一个唯一的会话令牌，使用两个 UUID 拼接而成
     * @return String 生成的会话令牌
     */
    public static String generateSessionToken() {
        // 生成两个 UUID 并拼接，去除中间的连字符
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }
}