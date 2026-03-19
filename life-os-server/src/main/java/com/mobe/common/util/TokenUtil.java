package com.mobe.common.util;

import java.util.UUID;

/**
 * 令牌工具类
 * <p>
 * 功能：提供生成各种令牌的工具方法
 * 说明：包含生成会话令牌等方法
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