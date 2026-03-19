package com.mobe.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域资源共享（CORS）配置类
 * <p>
 * 功能：配置跨域请求的规则，允许前端应用与后端API进行安全通信
 * 说明：实现 WebMvcConfigurer 接口，重写 addCorsMappings 方法来配置 CORS 规则
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /**
     * 配置跨域请求映射
     * <p>
     * 功能：设置跨域请求的规则，包括允许的源、方法、头部等
     * @param registry CORS 注册表，用于添加跨域映射规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的源地址，这里设置为前端开发服务器地址
                .allowedOrigins("http://localhost:3000")
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许携带凭证（如 cookies）
                .allowCredentials(true)
                // 预检请求的缓存时间（秒）
                .maxAge(3600);
    }
}