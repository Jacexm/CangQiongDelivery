package com.sky.config;

import com.sky.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * RestTemplate配置
 *
 * 为RestTemplate添加拦截器，在向其他微服务发送请求时自动传递TraceID
 * 实现分布式链路追踪
 *
 * @author CangQiong
 * @date 2025-12-20
 */
@Configuration
@Slf4j
public class RestTemplateConfig {

    /**
     * 配置RestTemplate，添加TraceID传递拦截器
     *
     * @return RestTemplate 配置完成的RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory())
        );

        // 添加TraceID传递拦截器
        restTemplate.getInterceptors().add(new TraceIdClientHttpRequestInterceptor());

        return restTemplate;
    }

    /**
     * TraceID客户端HTTP请求拦截器
     *
     * 在发送HTTP请求时，自动将当前线程的TraceID添加到请求头中
     */
    @Slf4j
    public static class TraceIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

        private static final String TRACE_ID_HEADER = "X-Trace-Id";

        @Override
        public ClientHttpResponse intercept(HttpRequest request,
                                           byte[] body,
                                           ClientHttpRequestExecution execution) throws IOException {
            // 获取当前线程的TraceID
            String traceId = TraceIdUtils.get();

            if (traceId != null && !traceId.isEmpty()) {
                // 将TraceID添加到请求头
                request.getHeaders().set(TRACE_ID_HEADER, traceId);
                log.debug("Added traceId to RestTemplate request header: {}", traceId);
            }

            // 继续执行请求
            return execution.execute(request, body);
        }
    }
}

