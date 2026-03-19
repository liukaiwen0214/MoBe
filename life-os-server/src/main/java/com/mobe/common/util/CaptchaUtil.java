package com.mobe.common.util;

import java.util.Random;

/**
 * 验证码工具类
 * <p>
 * 功能：提供生成验证码的工具方法
 * 说明：包含生成随机验证码的方法，使用指定的字符池
 */
public class CaptchaUtil {

    /**
     * 验证码字符池
     * <p>
     * 说明：包含大写字母和数字，排除了容易混淆的字符（如 I、O、0、1）
     */
    private static final String CHAR_POOL = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    
    /**
     * 随机数生成器
     */
    private static final Random RANDOM = new Random();

    /**
     * 私有构造方法，防止实例化
     */
    private CaptchaUtil() {
    }

    /**
     * 生成验证码
     * <p>
     * 功能：根据指定长度生成随机验证码
     * @param length 验证码长度
     * @return String 生成的验证码
     */
    public static String generateCaptchaCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            // 从字符池中随机选择字符
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }
}