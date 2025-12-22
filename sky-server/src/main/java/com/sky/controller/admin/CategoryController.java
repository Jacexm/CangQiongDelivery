package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@ApiOperation("分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param categoryDTO 分类信息
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> addCategory(@RequestBody CategoryDTO categoryDTO){
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> pageQueryCategory(CategoryPageQueryDTO categoryPageQueryDTO){
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id删除分类
     * @param id 分类id
     * @return
     */
    @DeleteMapping("/{id}")
    @ApiOperation("根据id删除分类")
    public Result<String> deleteById(@PathVariable Long id){
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类信息
     * @param categoryDTO 分类信息
     * @return
     */
    @PutMapping
    @ApiOperation("修改分类信息")
    public Result<String> updateCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 修改分类状态
     * @param status 分类状态
     * @param id 分类id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改分类状态")
    public Result<String> changeCategoryStatus(@PathVariable Integer status, @RequestParam Long id) {
        categoryService.changeCategoryStatus(status, id);
        return Result.success();
    }

    /**
     * 根据类型查询分类
     * @param type 分类类型
     * @return 分类列表
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> queryByType(@RequestParam Integer type){
//        log.info("根据类型查询分类：{}", type);
        List<Category> categoryList = categoryService.listByType(type);
        return Result.success(categoryList);
    }

}
