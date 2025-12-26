package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {
    /**
     * 根据分类id查询套餐及其包含的菜品信息
     *
     * @param setmeal 包含分类id和状态
     * @return 套餐列表
     */
    List<Setmeal> listByCategoryId(Setmeal setmeal);

    /**
     * 根据套餐id查询套餐包含的菜品信息
     *
     * @param setmealId 套餐id
     * @return 菜品列表
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

}
