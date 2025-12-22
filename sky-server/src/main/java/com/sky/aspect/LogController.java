package com.sky.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Aspect
@Component
@Slf4j
public class LogController {

    /**
     * 定义切入点：拦截 com.sky.controller 包下所有类的所有方法（包括多级子包）
     */
    @Pointcut("execution(* com.sky.controller..*(..))")
    public void controllerLogPointcut(){}

    /**
     * 环绕通知：记录方法执行时间和日志
     */
    @Around("controllerLogPointcut()")
    public Object aroundControllerLog(ProceedingJoinPoint joinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // 格式化参数，处理敏感信息
        String argsStr = formatArgs(args, methodName);

        log.info("【请求开始】{}.{}，参数：{}", className, methodName, argsStr);

        Object result;

        try{
            result = joinPoint.proceed();
        }catch (Throwable e){
            log.error("【请求异常】{}.{}，异常信息：{}", className, methodName, e.getMessage());
            throw e;
        }
        Long costTime = System.currentTimeMillis() - startTime;

        // 格式化返回结果，处理大数据量场景
        String resultStr = formatResult(result);
        log.info("【请求结束】{}.{}，耗时：{} ms，返回结果：{}", className, methodName, costTime, resultStr);

        return result;
    }

    /**
     * 格式化返回结果：处理大数据量和敏感信息
     */
    private String formatResult(Object result) {
        if (result == null) {
            return "null";
        }

        String resultStr = result.toString();

        // 如果结果过长（超过1000字符），进行截断处理
        if (resultStr.length() > 1000) {
            return resultStr.substring(0, 1000) + "... [已截断]";
        }

        return resultStr;
    }

    /**
     * 格式化参数：处理 MultipartFile 和敏感信息
     */
    private String formatArgs(Object[] args, String methodName) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        // 检查是否是查询或列表方法
        boolean isQueryMethod = isQueryMethod(methodName);

        // 检查是否是登录方法（敏感方法）
        boolean isSensitiveMethod = isSensitiveMethod(methodName);

        List<String> formattedArgs = new ArrayList<>();

        for (Object arg : args) {
            if (arg instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) arg;
                formattedArgs.add(String.format("MultipartFile(name=%s, size=%s bytes)",
                        file.getOriginalFilename(), file.getSize()));
            } else if (isQueryMethod && arg != null && !isPrimitiveType(arg)) {
                // 查询方法中，只显示对象类型名而不打印完整内容
                formattedArgs.add(arg.getClass().getSimpleName());
            } else if (isSensitiveMethod && arg != null) {
                // 敏感方法中，隐藏密码等敏感字段
                formattedArgs.add(maskSensitiveInfo(arg));
            } else {
                formattedArgs.add(arg != null ? arg.toString() : "null");
            }
        }

        return formattedArgs.toString();
    }

    /**
     * 判断是否是查询方法（分页查询、列表查询等）
     */
    private boolean isQueryMethod(String methodName) {
        return methodName.contains("query") || methodName.contains("list") ||
               methodName.contains("get") || methodName.contains("select") ||
               methodName.contains("find") || methodName.contains("page");
    }

    /**
     * 判断是否是基本类型或包装类型
     */
    private boolean isPrimitiveType(Object obj) {
        return obj instanceof String || obj instanceof Integer || obj instanceof Long ||
               obj instanceof Boolean || obj instanceof Double || obj instanceof Float;
    }

    /**
     * 判断是否是敏感方法（需要隐藏密码等信息）
     */
    private boolean isSensitiveMethod(String methodName) {
        return methodName.contains("login") || methodName.contains("register") ||
                methodName.contains("password") || methodName.contains("auth");
    }

    /**
     * 隐藏敏感信息（密码、token等）
     */
    private String maskSensitiveInfo(Object obj) {
        String objStr = obj.toString();

        // 隐藏 password 字段
        objStr = objStr.replaceAll("(?i)password['\"]?\\s*[:=]\\s*['\"]?[^,}'\"]+"
                , "password=***");

        // 隐藏 token 字段
        objStr = objStr.replaceAll("(?i)token['\"]?\\s*[:=]\\s*['\"]?[^,}'\"]+"
                , "token=***");

        // 隐藏 phone 字段（仅保留中间部分）
        objStr = objStr.replaceAll("(?i)phone['\"]?\\s*[:=]\\s*['\"]?(\\d{3})\\d{4}(\\d{4})['\"]?"
                , "phone=$1****$2");

        return objStr;
    }

}
