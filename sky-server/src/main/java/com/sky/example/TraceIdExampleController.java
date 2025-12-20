package com.sky.example;

import com.sky.result.Result;
import com.sky.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TraceID示例Controller
 *
 * 提供若干API端点，演示TraceID在HTTP请求中的使用
 *
 * API列表：
 * - GET /api/example/sync - 同步操作
 * - POST /api/example/async - 异步操作
 * - GET /api/example/microservice - 微服务调用
 * - GET /api/example/trace-id - 获取当前TraceID
 * - GET /api/example/exception - 异常处理演示
 * - GET /api/example/loop - 循环操作演示
 *
 * @author CangQiong
 * @date 2025-12-20
 */
@RestController
@RequestMapping("/api/example")
@Slf4j
public class TraceIdExampleController {

    @Autowired
    private TraceIdExampleService exampleService;

    /**
     * 示例1：同步操作
     *
     * curl http://localhost:8080/api/example/sync?userId=123
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @GetMapping("/sync")
    public Result syncExample(@RequestParam String userId) {
        log.info("Controller: Sync example received request for userId: {}", userId);
        exampleService.syncOperation(userId);
        log.info("Controller: Sync example completed");
        return Result.success("Sync operation completed");
    }

    /**
     * 示例2：异步操作
     *
     * curl -X POST http://localhost:8080/api/example/async?orderId=ORD-123
     *
     * @param orderId 订单ID
     * @return 操作结果（立即返回，异步处理）
     */
    @PostMapping("/async")
    public Result asyncExample(@RequestParam String orderId) {
        log.info("Controller: Async example received request for orderId: {}", orderId);
        exampleService.asyncOperation(orderId);
        log.info("Controller: Async example request accepted, processing in background");
        return Result.success("Async operation submitted, processing in background");
    }

    /**
     * 示例3：微服务调用
     *
     * curl http://localhost:8080/api/example/microservice?service=user-service
     *
     * @param service 微服务名称
     * @return 操作结果
     */
    @GetMapping("/microservice")
    public Result microserviceExample(@RequestParam String service) {
        log.info("Controller: Microservice example calling: {}", service);
        exampleService.callMicroservice("http://" + service + "/api/data");
        log.info("Controller: Microservice example completed");
        return Result.success("Microservice call completed");
    }

    /**
     * 示例4：获取当前TraceID
     *
     * curl http://localhost:8080/api/example/trace-id
     *
     * @return 当前TraceID
     */
    @GetMapping("/trace-id")
    public Result getTraceId() {
        String traceId = TraceIdUtils.get();
        log.info("Current traceId: {}", traceId);
        return Result.success(traceId);
    }

    /**
     * 示例5：异常处理演示
     *
     * curl "http://localhost:8080/api/example/exception?input=test"
     * curl "http://localhost:8080/api/example/exception?input="  # 会触发异常
     *
     * @param input 输入参数
     * @return 操作结果
     */
    @GetMapping("/exception")
    public Result exceptionExample(@RequestParam(required = false) String input) {
        log.info("Controller: Exception example with input: {}", input);
        try {
            exampleService.exceptionHandlingExample(input);
            return Result.success("Exception handling example completed");
        } catch (Exception e) {
            log.error("Error in exception example", e);
            return Result.error("Error occurred: " + e.getMessage());
        }
    }

    /**
     * 示例6：循环操作演示
     *
     * curl http://localhost:8080/api/example/loop
     *
     * @return 操作结果
     */
    @GetMapping("/loop")
    public Result loopExample() {
        log.info("Controller: Loop example started");
        String[] items = {"item1", "item2", "item3", "item4", "item5"};
        exampleService.loopOperationExample(items);
        log.info("Controller: Loop example completed");
        return Result.success("Loop operation completed for " + items.length + " items");
    }

    /**
     * 示例7：手动TraceID使用
     *
     * curl "http://localhost:8080/api/example/manual?resource=database"
     *
     * @param resource 资源名称
     * @return 操作结果
     */
    @GetMapping("/manual")
    public Result manualTraceIdExample(@RequestParam String resource) {
        log.info("Controller: Manual traceId example for resource: {}", resource);
        exampleService.manualTraceIdUsage(resource);
        log.info("Controller: Manual traceId example completed");
        return Result.success("Manual traceId usage completed");
    }

    /**
     * 示例8：TraceIdScope演示
     *
     * curl "http://localhost:8080/api/example/scope?data=testdata"
     *
     * @param data 数据
     * @return 操作结果
     */
    @GetMapping("/scope")
    public Result traceIdScopeExample(@RequestParam String data) {
        log.info("Controller: TraceIdScope example with data: {}", data);
        exampleService.traceIdScopeExample(data);
        log.info("Controller: TraceIdScope example completed");
        return Result.success("TraceIdScope example completed");
    }

    /**
     * 示例9：多次调用演示
     * 展示同一TraceID在多个操作中的使用
     *
     * curl http://localhost:8080/api/example/multi
     *
     * @return 操作结果
     */
    @PostMapping("/multi")
    public Result multiOperationsExample() {
        log.info("=== Multi-operations example started ===");

        try {
            // 操作1
            log.info("Step 1: Creating order");
            exampleService.syncOperation("user-123");

            // 操作2
            log.info("Step 2: Submitting async task");
            exampleService.asyncOperation("order-456");

            // 操作3
            log.info("Step 3: Getting current traceId");
            String traceId = TraceIdUtils.get();
            log.info("Current traceId in multi-operations: {}", traceId);

            // 操作4
            log.info("Step 4: Calling external service");
            exampleService.callMicroservice("external-service");

            log.info("=== Multi-operations example completed ===");
            return Result.success("Multi-operations completed successfully");

        } catch (Exception e) {
            log.error("Multi-operations example failed", e);
            return Result.error("Multi-operations failed: " + e.getMessage());
        }
    }
}

