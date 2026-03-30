package com.mobe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用主类
 * <p>
 * 文件用途：Spring Boot 应用的入口点，启动整个应用服务
 * 所属模块：life-os-server（后端服务模块）
 * 核心职责：初始化并启动 Spring Boot 应用，加载所有组件和配置
 * 与其他模块的关联：作为所有后端模块的启动入口，包括用户、习惯、财务、任务、清单等模块
 * 在整体业务流程中的位置：位于整个系统的最上层，负责启动和协调所有后端服务
 * 说明：使用 @SpringBootApplication 注解标记为 Spring Boot 应用，@EnableScheduling 启用定时任务功能
 */
@SpringBootApplication
@EnableScheduling
public class LifeOsServerApplication {

	/**
	 * 主方法
	 * <p>
	 * 功能：应用的入口点，启动 Spring Boot 应用
	 * @param args 命令行参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(LifeOsServerApplication.class, args);
	}

}
