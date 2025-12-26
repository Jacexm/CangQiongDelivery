package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;


    /**
     * 依据套餐id获取套餐内菜品信息
     * @param id 套餐id
     * @return 菜品列表
     */
    public  List<DishItemVO> getDishItemById(Long id){
        return setmealMapper.getDishItemBySetmealId(id);
    }



    /**
     * 依据categoryId获取套餐列表及其包含的菜品信息
     * @param categoryId 分类id
     * @return 套餐列表
     */
    public List<Setmeal> listSetmealByCategoryId(Long categoryId){
        Setmeal setmealQuery = Setmeal.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        List<Setmeal> setmeals = setmealMapper.listByCategoryId(setmealQuery);
        return setmeals;
        }
    }

