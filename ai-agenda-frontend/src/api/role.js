// src/api/role.js

import request from '@/utils/request';

// 获取角色列表
export const getRoleList = () => {
    return request({
        url: '/sys-role/list',
        method: 'get'
    });
}

// 新增角色
export const addRole = (data) => {
    return request({
        url: '/sys-role/add',
        method: 'post',
        data
    });
}

// 修改角色
export const updateRole = (data) => {
    return request({
        url: '/sys-role/update',
        method: 'put',
        data
    });
}

// 删除角色
export const deleteRole = (id) => {
    return request({
        url: `/sys-role/delete/${id}`,
        method: 'delete'
    });
}

// 获取角色已分配的菜单权限
export const getAssignedMenus = (roleId) => {
    return request({
        url: `/sys-role/assigned-menus/${roleId}`,
        method: 'get'
    });
}

// 分配菜单权限
export const assignMenus = (data) => {
    return request({
        url: '/sys-role/assign-menus',
        method: 'post',
        data
    });
}