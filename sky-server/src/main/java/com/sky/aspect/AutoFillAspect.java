package com.sky.aspect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.UserContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自动填充切面
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill))")
    public void autoFillPointCut(){}

    /**
     * 前置通知
     * @param joinPoint 切入点
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("公共字段的自动填充操作");

        // 获取被拦截的方法数据库操作对象
        MethodSignature signnature =  (MethodSignature)joinPoint.getSignature();
        // 获得方法上的注解对象
        AutoFill autoFill = signnature.getMethod().getAnnotation(AutoFill.class);
        // 获取注解对象的参数类型
        OperationType  operationType = autoFill.value();

        // 获取被拦截的方法参数
        Object[] args = joinPoint.getArgs();

        if(args == null || args.length == 0){
            return;
        }
        Object entity = args[0];

        LocalDateTime nowTime = LocalDateTime.now();
        Long currentUserId = UserContext.getCurrentId();

        try {
            switch (operationType){
                case INSERT:
                    Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                    Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                    Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                    setCreateTime.invoke(entity, nowTime);
                    setUpdateTime.invoke(entity, nowTime);
                    setCreateUser.invoke(entity, currentUserId);
                    setUpdateUser.invoke(entity, currentUserId);
                    break;

                case UPDATE:
                    Method setUpdateTime2 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                    Method setUpdateUser2 = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                    setUpdateTime2.invoke(entity, nowTime);
                    setUpdateUser2.invoke(entity, currentUserId);
                    break;
            }
        } catch (Exception e) {
            log.error("公共字段自动填充失败，异常信息：{}", e.getMessage());
            throw new RuntimeException("公共字段自动填充失败");
        }






    }

}
