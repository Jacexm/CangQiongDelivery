package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;


    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    public void addDishWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setImgUrl(dishDTO.getImage());

        dishMapper.insert(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        Long currentDishId = dish.getId();

        if (flavors != null && flavors.size() >0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(currentDishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }


}
