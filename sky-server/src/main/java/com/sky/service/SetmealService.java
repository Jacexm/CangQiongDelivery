package com.sky.service;


import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;


public interface SetmealService {

    /**
     * 根据分类id查询套餐及其包含的菜品信息
     * @param categoryId 分类id
     * @return 套餐列表
     */
    List<Setmeal> listSetmealByCategoryId(Long categoryId);

    /**
     * 根据套餐id查询套餐包含的菜品信息
     * @param id 套餐id
     * @return 菜品列表
     */
    List<DishItemVO> getDishItemById(Long id);
}
