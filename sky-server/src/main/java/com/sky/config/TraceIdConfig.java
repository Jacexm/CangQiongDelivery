package com.sky.config;

import com.sky.filter.TraceIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * TraceID过滤器配置
 *
 * 用于注册TraceIdFilter到Spring容器中，并设置其优先级
 * 确保TraceIdFilter在其他过滤器之前执行，以便为整个请求链路生成TraceID
 *
 * @author xtj
 * @date 2025-12-20
 */
@Configuration
public class TraceIdConfig {

    /**
     * 注册TraceID过滤器
     *
     * 设置：
     * - Filter实例：TraceIdFilter
     * - URL模式：所有请求(/*)
     * - 执行顺序：最高优先级(HIGHEST_PRECEDENCE)，确保最先执行
     *
     * @return FilterRegistrationBean 过滤器注册对象
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();

        // 设置过滤器实例
        registration.setFilter(new TraceIdFilter());

        // 设置URL模式：拦截所有请求
        registration.addUrlPatterns("/*");

        // 设置优先级：最高优先级，确保在其他过滤器之前执行
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registration;
    }
}

