package com.usedcar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;

/**
 * RestTemplate 配置
 * 用于调用 Python FastAPI 预测服务
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时 5 秒
        factory.setConnectTimeout(5000);
        // 读取超时 10 秒（模型预测可能需要时间）
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }
}