package com.mobe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用主类
 * <p>
 * 功能：Spring Boot 应用的入口点，启动整个应用
 * 说明：使用 @SpringBootApplication 注解标记为 Spring Boot 应用
 */
@SpringBootApplication
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
