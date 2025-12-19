package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增分类
     *
     * @param categoryDTO 分类信息
     */
    public void addCategory(CategoryDTO categoryDTO){
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        category.setStatus(StatusConstant.DISABLE);
        // 设置创建时间、创建人等信息
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insertCategory(category);
    }


    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO 分页查询参数
     * @return 分页结果
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO){
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        // 下一条SQL语句，自动加上limit关键词分页
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除分类
     *
     * @param id 分类id
     */
    public void deleteById(Long id){

        // 查询当前分类是否关联了菜品
        Integer count1 = dishMapper.countByCategoryId(id);
        if (count1 > 0){
            throw new RuntimeException("当前分类关联了菜品，不能删除");
        }

        // 查询当前分类是否关联了套餐
        Integer count2 = setmealMapper.countByCategoryId(id);
        if (count2 > 0){
            throw new RuntimeException("当前分类关联了套餐，不能删除");
        }

        // 正常删除分类
        categoryMapper.deleteById(id);

    }


    /**
     * 修改分类信息
     *
     * @param categoryDTO 分类信息
     */
    public void updateCategory(CategoryDTO categoryDTO){
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);

        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.updateCategoryById(category);
    }

    /**
     * 修改分类状态
     *
     * @param status 分类状态
     * @param id     分类id
     */
    public void changeCategoryStatus(Integer status, Long id){
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.updateCategoryById(category);
    }

    /**
     * 根据类型查询分类
     *
     * @param type 分类类型
     * @return 分类列表
     */
    public List<Category> listByType(Integer type){
        return categoryMapper.queryByType(type);

    }


}
