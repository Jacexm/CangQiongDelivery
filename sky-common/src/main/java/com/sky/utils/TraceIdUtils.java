package com.sky.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * TraceID工具类
 *
 * 用于在异步任务、多线程场景中传递和恢复TraceID
 * 因为新线程不会继承父线程的MDC信息，需要手动传递
 *
 * 使用场景：
 * 1. 异步任务处理（@Async）
 * 2. 线程池任务提交
 * 3. 消息队列消费
 * 4. 多线程调用
 *
 * 使用示例：
 * <pre>
 * // 在主线程中
 * TraceIdUtils.bind(traceId);
 * asyncService.doTask();  // 异步任务中可以访问到traceId
 *
 * // 或者使用try-with-resources自动清理
 * try (TraceIdScope scope = TraceIdUtils.scope(traceId)) {
 *     asyncService.doTask();
 * }
 * </pre>
 *
 * @author CangQiong
 * @date 2025-12-20
 */
@Slf4j
public class TraceIdUtils {

    /**
     * MDC中TraceID的键名
     */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 绑定TraceID到当前线程的MDC
     *
     * @param traceId TraceID
     */
    public static void bind(String traceId) {
        if (traceId != null && !traceId.isEmpty()) {
            MDC.put(TRACE_ID_KEY, traceId);
            log.debug("Bound traceId to MDC: {}", traceId);
        }
    }

    /**
     * 从MDC中获取TraceID
     *
     * @return TraceID，若不存在则返回null
     */
    public static String get() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 清除当前线程的TraceID
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
        log.debug("Cleared traceId from MDC");
    }

    /**
     * 清除MDC中所有信息
     */
    public static void clearAll() {
        MDC.clear();
        log.debug("Cleared all MDC information");
    }

    /**
     * 创建TraceID作用域，支持try-with-resources自动清理
     *
     * @param traceId TraceID
     * @return TraceIdScope对象
     */
    public static TraceIdScope scope(String traceId) {
        return new TraceIdScope(traceId);
    }

    /**
     * TraceID作用域类，用于自动管理TraceID的绑定和清理
     *
     * 使用示例：
     * <pre>
     * try (TraceIdScope scope = TraceIdUtils.scope(traceId)) {
     *     // 在此作用域内，TraceID被自动绑定到MDC
     *     log.info("Processing request");
     * }
     * // 离开作用域后，TraceID自动从MDC清除
     * </pre>
     */
    public static class TraceIdScope implements AutoCloseable {
        private final String traceId;

        public TraceIdScope(String traceId) {
            this.traceId = traceId;
            bind(traceId);
        }

        @Override
        public void close() {
            clear();
        }
    }
}

