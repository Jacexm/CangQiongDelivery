package com.sky.example;

import com.sky.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * TraceID使用示例服务
 *
 * 展示在各种场景下如何使用TraceID进行日志追踪
 *
 * @author CangQiong
 * @date 2025-12-20
 */
@Service
@Slf4j
public class TraceIdExampleService {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 示例1：基本同步操作
     * 日志会自动包含TraceID
     */
    public void syncOperation(String userId) {
        log.info("=== Example 1: 同步操作开始 ===");
        log.info("Processing user: {}", userId);

        try {
            // 模拟业务逻辑
            Thread.sleep(100);
            log.info("User processing completed successfully");
        } catch (InterruptedException e) {
            log.error("User processing interrupted", e);
        }
        log.info("=== Example 1: 同步操作结束 ===");
    }

    /**
     * 示例2：异步操作
     * 使用@Async注解，TraceID自动传递到异步线程
     */
    @Async("asyncExecutor")
    public void asyncOperation(String orderId) {
        log.info("=== Example 2: 异步操作开始 ===");
        log.info("Processing order asynchronously: {}", orderId);

        try {
            // 模拟长时间的异步操作
            Thread.sleep(500);
            log.info("Order processing completed asynchronously");
        } catch (InterruptedException e) {
            log.error("Async operation interrupted", e);
        }
        log.info("=== Example 2: 异步操作结束 ===");
    }

    /**
     * 示例3：微服务调用
     * RestTemplate会自动在请求头中添加TraceID
     */
    public void callMicroservice(String serviceUrl) {
        log.info("=== Example 3: 微服务调用开始 ===");
        log.info("Calling microservice: {}", serviceUrl);

        try {
            // 在这个请求中，X-Trace-Id头会自动被添加
            // ResponseEntity<String> response = restTemplate.getForEntity(serviceUrl, String.class);
            log.info("Microservice call completed");
        } catch (Exception e) {
            log.error("Microservice call failed", e);
        }
        log.info("=== Example 3: 微服务调用结束 ===");
    }

    /**
     * 示例4：手动获取和使用TraceID
     */
    public void manualTraceIdUsage(String resource) {
        log.info("=== Example 4: 手动使用TraceID开始 ===");

        // 获取当前的TraceID
        String traceId = TraceIdUtils.get();
        log.info("Current traceId: {}", traceId);

        // 模拟操作
        log.info("Processing resource: {}", resource);

        // 在需要时可以使用traceId进行特殊处理
        // 例如：存入数据库、写入外部系统等
        storeAuditLog(resource, traceId);

        log.info("=== Example 4: 手动使用TraceID结束 ===");
    }

    /**
     * 示例5：使用TraceIdScope自动管理
     */
    public void traceIdScopeExample(String data) {
        log.info("=== Example 5: TraceIdScope示例开始 ===");

        String traceId = TraceIdUtils.get();

        // 使用try-with-resources自动绑定和清理
        try (TraceIdUtils.TraceIdScope scope = TraceIdUtils.scope(traceId)) {
            log.info("Inside scope, processing: {}", data);
            // TraceID自动在此scope中可用
            innerOperation();
        }

        log.info("=== Example 5: TraceIdScope示例结束 ===");
    }

    /**
     * 示例6：异常处理中的TraceID
     */
    public void exceptionHandlingExample(String input) {
        log.info("=== Example 6: 异常处理示例开始 ===");

        try {
            log.info("Processing input: {}", input);

            if (input == null || input.isEmpty()) {
                throw new IllegalArgumentException("Input cannot be empty");
            }

            // 模拟处理
            processData(input);
            log.info("Processing completed successfully");

        } catch (IllegalArgumentException e) {
            // 异常日志自动包含TraceID，便于问题追踪
            log.error("Validation error occurred", e);
            // 可以将TraceID存入错误表
            storeErrorLog(input, e, TraceIdUtils.get());

        } catch (Exception e) {
            log.error("Unexpected error occurred", e);
            storeErrorLog(input, e, TraceIdUtils.get());
        }

        log.info("=== Example 6: 异常处理示例结束 ===");
    }

    /**
     * 示例7：循环操作中的TraceID
     */
    public void loopOperationExample(String[] items) {
        log.info("=== Example 7: 循环操作示例开始 ===");
        log.info("Processing {} items", items.length);

        for (int i = 0; i < items.length; i++) {
            log.info("Processing item {} of {}: {}", i + 1, items.length, items[i]);
            try {
                processItem(items[i]);
                log.debug("Item {} processed successfully", i + 1);
            } catch (Exception e) {
                log.error("Error processing item {}", i + 1, e);
            }
        }

        log.info("=== Example 7: 循环操作示例结束 ===");
    }

    /**
     * 内部方法：模拟数据处理
     */
    private void processData(String data) {
        log.debug("In processData, handling: {}", data);
        // 处理逻辑
    }

    /**
     * 内部方法：处理单个项目
     */
    private void processItem(String item) {
        log.debug("Processing individual item: {}", item);
        // 处理逻辑
    }

    /**
     * 内部方法：内部操作
     */
    private void innerOperation() {
        log.debug("Executing inner operation");
        // 操作逻辑
    }

    /**
     * 内部方法：存储审计日志
     * 实际应用中会将日志存入数据库
     */
    private void storeAuditLog(String resource, String traceId) {
        log.debug("Storing audit log for resource: {} with traceId: {}", resource, traceId);
        // 存储逻辑
    }

    /**
     * 内部方法：存储错误日志
     * 实际应用中会将错误信息存入数据库便于查询
     */
    private void storeErrorLog(String input, Exception e, String traceId) {
        log.debug("Storing error log - input: {}, traceId: {}, error: {}",
                 input, traceId, e.getMessage());
        // 存储逻辑：
        // INSERT INTO error_log (input, trace_id, error_message, created_time)
        // VALUES (?, ?, ?, NOW())
    }
}

