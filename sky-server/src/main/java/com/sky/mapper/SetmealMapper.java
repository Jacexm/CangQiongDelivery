package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类ID查询套餐的数量
     * @param id
     * @return java.lang.Integer
     **/
    @Select("select count(id) from sky_take_out.setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 新增套餐
     * @param setmeal
     * @return void
     **/
    @AutoFill(value = OperationType.INSERT)
    void save(Setmeal setmeal);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return com.github.pagehelper.Page<com.sky.vo.SetmealVO>
     **/
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据ID查询套餐
     * @param id
     * @return com.sky.vo.SetmealVO
     **/
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 更新套餐信息
     * @param setmeal
     * @return void
     **/
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据ID删除套餐信息
     * @param id
     * @return void
     **/
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long id);


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

    /**
     * 根据状态统计数量
     * @param paramMap
     * @return java.lang.Integer
     **/
    Integer getCount(Map<String, Integer> paramMap);

}
