// src/api/ai-message.js

import request from '@/utils/request';

// 获取AI会话消息列表
export const getAiMessageList = (params) => {
    return request({
        url: '/ai-message/list',
        method: 'get',
        params
    });
}

// 新增AI会话消息
export const addAiMessage = (data) => {
    return request({
        url: '/ai-message/add',
        method: 'post',
        data
    });
}

// 修改AI会话消息
export const updateAiMessage = (data) => {
    return request({
        url: '/ai-message/update',
        method: 'put',
        data
    });
}

// 删除AI会话消息
export const deleteAiMessage = (id) => {
    return request({
        url: `/ai-message/delete/${id}`,
        method: 'delete'
    });
}