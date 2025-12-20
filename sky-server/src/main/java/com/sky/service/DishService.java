package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.result.Result;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    void addDishWithFlavor(DishDTO dishDTO);

}
