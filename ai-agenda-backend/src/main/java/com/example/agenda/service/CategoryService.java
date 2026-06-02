package com.example.agenda.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agenda.pojo.entity.Category;

public interface CategoryService extends IService<Category> {
    /**
     * 根据名称查询分类ID
     * @param userId 用户ID
     * @param name 分类名称 (如：工作、生活)
     * @return 分类ID，找不到返回 null
     */
    Long getCategoryIdByName(Long userId, String name);
}