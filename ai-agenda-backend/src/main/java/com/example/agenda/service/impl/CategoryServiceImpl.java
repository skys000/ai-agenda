package com.example.agenda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agenda.mapper.CategoryMapper;
import com.example.agenda.pojo.entity.Category;
import com.example.agenda.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public Long getCategoryIdByName(Long userId, String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        // 查询逻辑：名称匹配 且 (所属用户是当前用户 或 是公共分类0)
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, name)
                .and(w -> w.eq(Category::getUserId, userId).or().eq(Category::getUserId, 0L))
                .last("LIMIT 1"); // 防止有多条数据报错

        Category category = this.getOne(queryWrapper);
        return category != null ? category.getId() : null;
    }
}