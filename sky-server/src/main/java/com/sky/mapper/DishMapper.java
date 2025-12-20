package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    /**
     * 根据分类id查询套餐数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) form fish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);
}
