package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);


    /**
     * 根据菜品id查询套餐id集合
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);



    /**
     * 新增套餐与菜品对应关系
     * @param setmealDishes
     * @return void
     **/
    void saveWithDish(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐ID查询其对应菜品列表
     * @param setmealId
     * @return java.util.List<com.sky.entity.SetmealDish>
     **/
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);

    /**
     * 根据套餐ID删除对应的菜品信息
     * @param setmealId
     * @return void
     * @author paxi
     * @data 2023/9/3
     **/
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);

}


