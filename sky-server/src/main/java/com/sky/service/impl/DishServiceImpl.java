package com.sky.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisKeysConstant;
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
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final ObjectMapper CACHE_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final TypeReference<List<DishVO>> DISHVO_LIST_TYPE = new TypeReference<List<DishVO>>() {};

    /**
     * 根据菜品分类id生成菜品缓存key
     * @param categoryId
     * @return
     */
    private String keyOfCategory(Long categoryId) {
        return RedisKeysConstant.DISH_BY_CATERGORYID_KEY + categoryId;
    }

    /**
     * 事务提交后删除菜品分类缓存
     * @param categoryIds
     */
    private void evictCategoryKeysAfterCommit(Collection<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return;
        // 事务提交后再删缓存，避免数据库回滚但缓存已删
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (Long cid : new HashSet<>(categoryIds)) {
                        String key = keyOfCategory(cid);
                        try {
                            redisTemplate.delete(key);
                            log.debug("Evict dish cache after commit, key={}", key);
                        } catch (Exception e) {
                            log.warn("Evict dish cache failed, key={}", key, e);
                        }
                    }
                }
            });
        } else {
            // 无事务时直接删除
            for (Long cid : new HashSet<>(categoryIds)) {
                redisTemplate.delete(keyOfCategory(cid));
            }
        }
    }


    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @Transactional
    public void addDishWithFlavor(DishDTO dishDTO){
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 修复错误的变量名，设置图片URL
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
        //新增菜品后，清理缓存
        evictCategoryKeysAfterCommit(Collections.singleton(dish.getCategoryId()));
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
    @Transactional
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

        // 先查询待删除菜品所属分类，避免删除后再查为空
        Set<Long> affectedCategoryIds = ids.stream()
                .map(dishMapper::getById)
                .filter(Objects::nonNull)
                .map(Dish::getCategoryId)
                .collect(Collectors.toSet());

        // 执行删除（仅一次）
        for(Long id : ids){
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }

        // 事务提交后删除缓存
        evictCategoryKeysAfterCommit(affectedCategoryIds);
    }
    /**
     * 修改菜品状态
     * @param status
     * @param id
     */
    @Transactional
    public void updateStatus(Integer status, Long id){
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.updateDishStatusById(dish);

        Set<Long> toEvict = new HashSet<>();
        Dish updatedStatusDish = dishMapper.getById(id);
        if(updatedStatusDish != null && updatedStatusDish.getCategoryId() != null) {
            toEvict.add(updatedStatusDish.getCategoryId());
        }
        // 删除redis缓存数据
        evictCategoryKeysAfterCommit(toEvict);
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
    @Transactional
    public void updateDishWithFlavor(DishDTO dishDTO){
        // 读取原分类以便分类变更时双key失效
        Dish originalDish = dishMapper.getById(dishDTO.getId());
        Long originalCategoryId = originalDish != null ? originalDish.getCategoryId() : null;


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

        //若变更，使当前菜品分类与原分类的缓存都失效
        Set<Long> toEvict = new HashSet<>();
        if(originalCategoryId != null){
            toEvict.add(originalCategoryId);
        }
        if(dish.getCategoryId() != null){
            toEvict.add(dish.getCategoryId());
        }
        evictCategoryKeysAfterCommit(toEvict);
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

    /**
     * 根据key获取菜品缓存
     * @param key
     * @return
     */
    public List<DishVO> getDishCacheByKey(String key){
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) {
            return null;
        }
        if (val instanceof List<?>) {
            List<?> raw = (List<?>) val;
            if (raw.isEmpty()) {
                return Collections.emptyList();
            }
            Object first = raw.get(0);
            if (first instanceof DishVO) {
                return (List<DishVO>) raw;
            }
            if (first instanceof Map) {
                try {
                    String json = CACHE_MAPPER.writeValueAsString(raw);
                    List<DishVO> list = CACHE_MAPPER.readValue(json, DISHVO_LIST_TYPE);
                    log.info("Redis缓存元素为 Map，已转换为 List<DishVO>，key: {}", key);
                    return list;
                } catch (Exception e) {
                    log.warn("Redis缓存 Map -> DishVO 转换失败，key: {}, err: {}", key, e.getMessage());
                    return null;
                }
            }
            log.warn("Redis缓存元素类型不匹配，key: {}, 实际元素类型: {}", key, first.getClass().getName());
            return null;
        }
        log.warn("Redis缓存类型不匹配，key: {}, 实际类型: {}", key, val.getClass().getName());
        return null;
    }


    /**
     * 根据key设置菜品缓存
     * @param key
     * @param dishVOs
     */
    public void setDishCacheByKey(String key, List<DishVO> dishVOs){
        if (dishVOs == null) {
            // 允许写入null则直接删除缓存，避免存入null带来的歧义
            redisTemplate.delete(key);
            return;
        }
        redisTemplate.opsForValue().set(key, dishVOs);

    }

    public void clearDishCache(String pattern){
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }


}
