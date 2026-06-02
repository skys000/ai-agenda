// src/api/user.js

import request from '@/utils/request';

/**
 * 登录API
 * @param {object} data 包含 username 和 password
 * @returns Promise
 */
export const loginApi = (data) => {
    return request({
        url: '/sys-user/login',
        method: 'post',
        data
    });
}

// 获取用户列表
export const getUserList = (params) => {
    return request({
        url: '/sys-user/list',
        method: 'get',
        params
    });
}

// 新增用户
export const addUser = (data) => {
    return request({
        url: '/sys-user/add',
        method: 'post',
        data
    });
}

// 修改用户
export const updateUser = (data) => {
    return request({
        url: '/sys-user/update',
        method: 'put',
        data
    });
}

// 删除用户
export const deleteUser = (id) => {
    return request({
        url: `/sys-user/delete/${id}`,
        method: 'delete'
    });
}

// 获取用户已分配的角色
export const getAssignedRoles = (userId) => {
    return request({
        url: `/sys-user/assigned-roles/${userId}`,
        method: 'get'
    });
}

// 为用户分配角色
export const assignRoles = (data) => {
    return request({
        url: '/sys-user/assign-roles',
        method: 'post',
        data
    });
}