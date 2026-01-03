package com.sky.controller.user;


import com.sky.constant.RedisKeysConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "用户端-菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;


    @GetMapping("/list")
    @ApiOperation("依据categoryId获取菜品列表")
    public Result<List<DishVO>> listDishes(@RequestParam Long categoryId){

        String dishKey = RedisKeysConstant.DISH_BY_CATERGORYID_KEY + categoryId;

        List<DishVO> dishCaches = dishService.getDishCacheByKey(dishKey);
        if(dishCaches != null && !dishCaches.isEmpty()){
            log.info("从缓存中获取菜品列表，categoryId：{}", categoryId);
            return Result.success(dishCaches);
        }
        List<DishVO> dishVOs = dishService.listDishesByCategoryId(categoryId);
        dishService.setDishCacheByKey(dishKey, dishVOs);

        return Result.success(dishVOs);
    }


}
