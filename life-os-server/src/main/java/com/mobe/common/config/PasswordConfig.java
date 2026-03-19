package com.mobe.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码配置类
 * <p>
 * 功能：配置密码编码器，用于密码的加密和验证
 * 说明：使用 BCryptPasswordEncoder 作为密码编码器，提供安全的密码哈希功能
 */
@Configuration
public class PasswordConfig {

    /**
     * 配置密码编码器
     * <p>
     * 功能：创建并返回 BCryptPasswordEncoder 实例，用于密码的加密和验证
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}