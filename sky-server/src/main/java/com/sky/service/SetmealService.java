package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
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
     * 根据ID查询套餐
     * @param id
     * @return com.sky.vo.SetmealVO
     **/
    SetmealVO getByIdWithDish(Long id);

    /**
     * 根据套餐id查询套餐包含的菜品信息
     * @param id 套餐id
     * @return 菜品列表
     */
    List<DishItemVO> getDishItemById(Long id);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return com.sky.result.PageResult
     **/
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);


    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return void
     **/
    void update(SetmealDTO setmealDTO);

    /**
     * 起售和禁售套餐
     * @param status
     * @param id
     * @return void
     **/
    void startAndStop(Integer status, Long id);

    /**
     * 根据ID批量删除套餐
     * @param ids
     * @return void
     **/
    void deleteByIds(List<Long> ids);


}
