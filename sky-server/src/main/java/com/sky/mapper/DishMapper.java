package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {
    /**
     * 根据分类id查询套餐数量
     * @param categoryId
     * @return
     */
    @Select("select count(*) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);



    @Insert("insert into dish (name, category_id, price, img_url, description, create_time, update_time, create_user, update_user) values" +
            "(#{name}, #{categoryId}, #{price}, #{imgUrl}, #{description}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
}
