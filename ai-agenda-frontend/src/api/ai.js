// src/api/ai.js

import request from '@/utils/request';

// 普通AI聊天（非流式）
export const chatWithAI = (params) => {
    return request({
        url: '/ai/chat',
        method: 'get',
        params
    });
}

// 流式AI聊天
export const streamChatWithAI = (params) => {
    return request({
        url: '/ai/stream',
        method: 'get',
        params
    });
}