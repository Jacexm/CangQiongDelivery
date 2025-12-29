package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    void addDishWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteDish(List<Long> ids);

    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    void updateStatus(Integer status, Long id);


    /**
     * 根据id查询菜品信息
     * @param id
     * @return DishDTO
     */
    DishVO getByIdWithFlavor(Long id);


    /**
     * 修改菜品信息
     * @param dishDTO
     */
    void updateDishWithFlavor(DishDTO dishDTO);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<DishVO> listDishesByCategoryId(Long categoryId);

    /**
     * 根据key获取菜品缓存
     * @param key
     * @return
     */
    List<DishVO> getDishCacheByKey(String key);

    /**
     * 根据key设置菜品缓存
     * @param key
     * @param dishVOs
     */
    void setDishCacheByKey(String key, List<DishVO> dishVOs);




    void clearDishCache(String pattern);

}
