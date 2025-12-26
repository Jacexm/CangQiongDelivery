package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    public void addDishWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setImgUrl(dishDTO.getImage());

        dishMapper.insert(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        Long currentDishId = dish.getId();

        if (flavors != null && flavors.size() >0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(currentDishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO){
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        log.debug("菜品分页查询结果：{}", page.getResult());
        return new PageResult(page.getTotal(), page);
    }

    /**
     * 批量删除菜品
     * <pre>
     * - 起售中的菜品不能删除
     * - 被套餐关联的菜品不能删除
     * - 删除菜品后，关联的口味数据也需要删除掉
     * </pre>
     * @param ids
     */
    public void deleteDish(List<Long> ids){
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);

            if(dish == null){
                log.error("删除菜品失败，菜品不存在，id：{}", id);
                throw new RuntimeException("删除菜品失败，菜品不存在");
            }
            if(dish.getStatus() == StatusConstant.ENABLE){
                log.error("删除菜品失败，菜品起售中，name：{}", dish.getName());
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否能够删除---是否被套餐关联了？？
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for(Long id : ids){
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }
    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    public void updateStatus(Integer status, Long id){
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.updateDishStatusById(dish);
    }


    /**
     * 根据id查询菜品信息
     * @param id
     * @return DishDTO
     */
    public DishVO getByIdWithFlavor(Long id){
        Dish dish = dishMapper.getById(id);

        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setImage(dish.getImgUrl());
        dishVO.setFlavors(dishFlavors);

        return dishVO;

    }


    /**
     * 修改菜品信息
     * @param dishDTO
     */
    public void updateDishWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setImgUrl(dishDTO.getImage());

        dishMapper.updateDishById(dish);

        //先删除原有的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //再添加新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        Long currentDishId = dishDTO.getId();

        if (flavors != null && flavors.size() >0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(currentDishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<DishVO> listDishesByCategoryId(Long categoryId){
        Dish dishQuery = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Dish> dishes = dishMapper.getByCategoryId(dishQuery);
        List<DishVO> dishVOList = new ArrayList<>();
        for(Dish dish : dishes){
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            dishVO.setImage(dish.getImgUrl());
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(dish.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
    }
        return dishVOList;
    }


}
