package com.sky.config;

import com.sky.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务执行器配置
 *
 * 配置线程池执行器以支持异步任务，并在任务执行时自动传递TraceID
 * 通过包装Runnable任务来实现TraceID的跨线程传递
 *
 * @author CangQiong
 * @date 2025-12-20
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncTaskExecutorConfig {

    /**
     * 配置异步任务执行器
     *
     * 线程池参数说明：
     * - corePoolSize: 核心线程数（8个）
     * - maxPoolSize: 最大线程数（16个）
     * - queueCapacity: 队列容量（100）
     * - keepAliveSeconds: 线程空闲保活时间（60秒）
     * - threadNamePrefix: 线程名称前缀
     *
     * @return Executor 异步任务执行器
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 线程池配置
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-task-");

        // 设置拒绝策略为使用调用者线程运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 设置任务装饰器，自动传递TraceID
        executor.setTaskDecorator(new TraceIdTaskDecorator());

        executor.initialize();
        log.info("Async task executor initialized with core pool size: 8, max pool size: 16");

        return executor;
    }

    /**
     * 任务装饰器，用于在异步任务执行前后传递和清理TraceID
     */
    @Slf4j
    public static class TraceIdTaskDecorator implements org.springframework.core.task.TaskDecorator {

        @Override
        public Runnable decorate(Runnable runnable) {
            // 获取当前线程的TraceID
            String traceId = TraceIdUtils.get();

            // 返回装饰后的Runnable，在执行时恢复TraceID
            return () -> {
                try {
                    // 在新线程中绑定TraceID
                    TraceIdUtils.bind(traceId);
                    log.debug("Restored traceId in async task: {}", traceId);

                    // 执行原始任务
                    runnable.run();
                } finally {
                    // 任务完成后清理TraceID
                    TraceIdUtils.clear();
                }
            };
        }
    }
}

