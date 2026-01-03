package com.sky.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * TraceID过滤器 - 企业级实现
 * 为每个HTTP请求生成唯一的TraceID，并通过MDC机制传递到日志系统
 *
 * 功能：
 * 1. 检查请求头中是否存在X-Trace-Id
 * 2. 若不存在则生成新的UUID格式的TraceID
 * 3. 将TraceID存入MDC供日志框架使用（在整个请求生命周期中保持）
 * 4. 将TraceID添加到响应头便于客户端追踪
 * 5. 支持异步任务和后续操作获取TraceID
 * 6. 仅在HTTP请求完全离开容器时清理MDC，防止内存泄漏
 *
 * TraceID生命周期：
 * ┌─────────────────────────────────────────────────┐
 * │ HTTP请求进入                                    │
 * │   ↓                                              │
 * │ Filter: 生成/提取TraceID                       │
 * │   ↓                                              │
 * │ MDC.put(traceId)  ← TraceID在此设置             │
 * │   ↓                                              │
 * │ 业务处理（Service/Controller）                 │
 * │   ├─ 同步操作（日志自动含TraceID）             │
 * │   ├─ 异步任务（自动传递TraceID）               │
 * │   └─ 微服务调用（自动添加请求头）              │
 * │   ↓                                              │
 * │ HTTP响应返回（此时TraceID仍在MDC）             │
 * │   ↓                                              │
 * │ finally: 请求完全离开容器                      │
 * │   ↓                                              │
 * │ MDC.clear()  ← TraceID在此清理                  │
 * │   ↓                                              │
 * │ 请求完全结束                                    │
 * └─────────────────────────────────────────────────┘
 *
 * 重要说明：
 * - TraceID在MDC中的保留时间 = 整个HTTP请求的处理时间
 * - 异步任务、后续消息处理需要在任务开始时手动绑定TraceID
 * - 微服务调用会自动在请求头中添加TraceID，接收方会生成相同的TraceID
 * - finally块执行时，才是真正的清理时机
 *
 * @author CangQiong
 * @date 2025-12-20
 * @version 2.0
 */
@Slf4j
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * 请求头中TraceID的名称（符合OpenTelemetry标准）
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * MDC中TraceID的键名
     */
    private static final String TRACE_ID_KEY = "traceId";

    /**
     * 请求属性名，用于在请求对象中保存TraceID，以便后续异步操作获取
     */
    private static final String TRACE_ID_REQUEST_ATTR = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 从请求头获取TraceID
        String traceId = request.getHeader(TRACE_ID_HEADER);
        boolean isNewTraceId = false;

        if (traceId == null || traceId.isEmpty()) {
            // 生成新的TraceID：UUID格式，去除中划线使其更紧凑
            // 生成的TraceID格式: 32个字符的十六进制字符串
            traceId = UUID.randomUUID().toString().replace("-", "");
            isNewTraceId = true;
            log.debug("Generated new TraceID: {}", traceId);
        } else {
            log.debug("Received TraceID from request header: {}", traceId);
        }

        try {
            // 步骤1: 将TraceID放入MDC
            MDC.put(TRACE_ID_KEY, traceId);
            log.debug("TraceID bound to MDC: {} (isNew: {})", traceId, isNewTraceId);

            // 步骤2: 将TraceID添加到响应头
            // 客户端可以通过响应头获取TraceID，便于端到端追踪
            response.setHeader(TRACE_ID_HEADER, traceId);

            // 步骤3: 保存TraceID到请求对象
            // 便于在异步任务、事件监听器等后续操作中获取
            request.setAttribute(TRACE_ID_REQUEST_ATTR, traceId);

            // 步骤4: 继续执行过滤链
            // 此时所有业务处理（Service/Controller）都将使用此TraceID
            // 异步任务会自动继承此TraceID（通过TaskDecorator）
            // 微服务调用会自动添加X-Trace-Id请求头（通过RestTemplate拦截器）
            log.debug("Entering filter chain with TraceID: {}", traceId);
            filterChain.doFilter(request, response);
            log.debug("Exiting filter chain with TraceID: {}", traceId);

        } finally {
            // 步骤5: 清理MDC
            // 重要：此时HTTP请求已完全处理，即将离开容器
            // 这是清理MDC的正确时机
            //
            // 注意事项：
            // 1. 不在此处清理不是"泄漏"，而是正确的设计
            // 2. 异步任务需要的TraceID已通过TaskDecorator传递
            // 3. 消息队列需要的TraceID需在生产者端手动传递
            // 4. ThreadPool的线程复用需要在任务装饰器中处理

            MDC.remove(TRACE_ID_KEY);

            // 可选：同时清理可能的其他MDC值，防止线程池复用时的信息污染
            // 注意：这里使用remove而不是clear，只清理traceId，保留其他业务上下文

            log.debug("TraceID removed from MDC: {}", traceId);
        }
    }

    /**
     * 获取当前请求的TraceID
     *
     * 用于在Filter链中后续的请求处理器中获取TraceID
     *
     * @param request HTTP请求对象
     * @return TraceID字符串，如果不存在则返回null
     */
    public static String getTraceId(HttpServletRequest request) {
        Object traceId = request.getAttribute(TRACE_ID_REQUEST_ATTR);
        return traceId != null ? traceId.toString() : null;
    }
}

