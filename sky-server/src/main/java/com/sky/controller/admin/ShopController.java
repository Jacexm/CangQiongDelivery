package com.sky.controller.admin;


import com.sky.context.UserContext;
import com.sky.result.Result;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Tag(name = "店铺相关接口")
public class ShopController {
    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 设置店铺的营业状态
     * @param status 店铺状态 0-打烊中 1-营业中
     * @return
     */
    @PutMapping("/{status}")
    @Operation(summary = "设置店铺的营业状态")
    public Result updateShopStatus(@PathVariable Integer status){
        log.info("用户{}设置店铺的营业状态：{}", UserContext.getCurrentId(), status==1 ? "营业中":"打烊中");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }
    /**
     * 获取店铺的营业状态
     * @return 店铺状态 0-打烊中 1-营业中
     */
    @GetMapping("/status")
    @Operation(summary = "获取店铺的营业状态")
    public Result<Integer> getShopStatus(){
        Integer status = 0; // 默认值为打烊中

        try {
            Object obj = redisTemplate.opsForValue().get(KEY);
            status = obj != null ? (Integer) obj : 0;
        } catch (Exception e) {
            log.warn("获取Redis店铺状态异常，使用默认值（打烊中）", e);
            status = 0;
        }

        log.info("用户{} 获取店铺的营业状态：{}", UserContext.getCurrentId(), status==1 ? "营业中":"打烊中");
        return Result.success(status);
    }




}
