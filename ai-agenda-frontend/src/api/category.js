// src/api/category.js

import request from '@/utils/request';

// 获取分类列表
export const getCategoryList = () => {
    return request({
        url: '/category/list',
        method: 'get'
    });
}

// 新增分类
export const addCategory = (data) => {
    return request({
        url: '/category/add',
        method: 'post',
        data
    });
}

// 修改分类
export const updateCategory = (data) => {
    return request({
        url: '/category/update',
        method: 'put',
        data
    });
}

// 删除分类
export const deleteCategory = (id) => {
    return request({
        url: `/category/delete/${id}`,
        method: 'delete'
    });
}