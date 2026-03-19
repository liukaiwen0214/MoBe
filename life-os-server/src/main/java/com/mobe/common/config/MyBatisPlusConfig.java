package com.mobe.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 * <p>
 * 功能：配置 MyBatis Plus 相关参数，包括 Mapper 扫描路径和元对象处理器
 * 说明：使用 @Configuration 注解标记为配置类，@MapperScan 指定 Mapper 接口扫描路径
 */
@Configuration
@MapperScan("com.mobe")
public class MyBatisPlusConfig {
    
    /**
     * 配置元对象处理器
     * <p>
     * 功能：注册自定义的元对象处理器，用于自动填充创建时间、更新时间等字段
     * @return MetaObjectHandler 元对象处理器实例
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }
}