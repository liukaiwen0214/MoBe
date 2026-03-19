package com.mobe.common.config;

import com.mobe.common.interceptor.RequestLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类
 * <p>
 * 功能：配置 Spring MVC 相关参数，包括拦截器的注册
 * 说明：实现 WebMvcConfigurer 接口，重写 addInterceptors 方法来注册拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     * <p>
     * 功能：添加请求日志拦截器，用于记录 HTTP 请求的详细信息
     * @param registry 拦截器注册表，用于添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLogInterceptor())
                // 拦截所有路径
                .addPathPatterns("/**")
                // 排除不需要拦截的路径
                .excludePathPatterns(
                        "/error",  // 错误页面
                        "/favicon.ico"  // 网站图标
                );
    }
}