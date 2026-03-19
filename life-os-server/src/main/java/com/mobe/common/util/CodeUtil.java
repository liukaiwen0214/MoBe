package com.mobe.common.util;

import java.util.Random;

/**
 * 代码生成工具类
 * <p>
 * 功能：提供生成各种代码的工具方法
 * 说明：包含生成数字验证码等方法
 */
public class CodeUtil {

    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();

    /**
     * 私有构造方法，防止实例化
     */
    private CodeUtil() {
    }

    /**
     * 生成数字验证码
     * <p>
     * 功能：根据指定长度生成随机数字验证码
     * @param length 验证码长度
     * @return String 生成的数字验证码
     */
    public static String generateNumericCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            // 生成 0-9 之间的随机数字
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}