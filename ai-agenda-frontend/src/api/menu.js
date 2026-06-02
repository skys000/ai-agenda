// src/api/menu.js

import request from '@/utils/request'; // <--- 必须是这个！

// 获取菜单树
export const getMenuTree = () => {
    return request({
        url: '/sys-menu/tree',
        method: 'get'
    });
}

// 新增菜单
export const addMenu = (data) => {
    return request({
        url: '/sys-menu/add',
        method: 'post',
        data
    });
}

// 修改菜单
export const updateMenu = (data) => {
    return request({
        url: '/sys-menu/update',
        method: 'put',
        data
    });
}

// 删除菜单
export const deleteMenu = (id) => {
    return request({
        url: `/sys-menu/delete/${id}`,
        method: 'delete'
    });
}