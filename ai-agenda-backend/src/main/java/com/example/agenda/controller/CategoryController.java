package com.example.agenda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agenda.pojo.entity.Category;
import com.example.agenda.result.Result;
import com.example.agenda.service.CategoryService;
import com.example.agenda.util.BaseContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "分类管理")
@RestController
@RequestMapping("/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "查询所有分类(公共+个人)")
    @GetMapping("/list")
    public Result<List<Category>> list() {
        // 获取当前用户ID
        Long currentUserId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Category> query = new LambdaQueryWrapper<>();
        // 逻辑：userId = 0 (公共) OR userId = currentUserId (我的)
        query.eq(Category::getUserId, 0L)
                .or()
                .eq(Category::getUserId, currentUserId);

        return Result.success(categoryService.list(query));
    }

    @Operation(summary = "新增个人分类")
    @PostMapping("/add")
    public Result<String> add(@RequestBody Category category) {
        category.setUserId(BaseContext.getCurrentId()); // 绑定给当前用户
        categoryService.save(category);
        return Result.success("添加成功");
    }
    @Operation(summary = "修改分类")
    @PutMapping("/update")
    public Result<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 实际开发中应判断该分类是否被日程关联
        categoryService.removeById(id);
        return Result.success("删除成功");
    }
}