// src/utils/request.js

import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router'; // 引入路由实例

// 创建axios实例
const service = axios.create({
    // 注意：vite中通过 import.meta.env 获取环境变量
    baseURL: import.meta.env.VITE_APP_BASE_API, 
    // 移除超时限制，让AI可以慢慢返回
});

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 在发送请求前，从 localStorage 获取 token
        const token = localStorage.getItem('token');
        if (token) {
            // 让每个请求携带自定义token
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        return config;
    },
    error => {
        console.log(error); // for debug
        return Promise.reject(error);
    }
);

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data;
        // 假设后端的统一响应结果集中，code为20000代表成功
        if (res.code !== 200) {
            ElMessage({
                message: res.message || 'Error',
                type: 'error',
                duration: 5 * 1000
            });

            // 50008: 非法Token; 50012: 其他客户端已登录; 50014: Token过期;
            // 这是一个示例，你可以根据后端情况调整
            if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
                // TODO: 可以弹出确认框提示用户
                localStorage.removeItem('token');
                router.push('/login');
            }

            return Promise.reject(new Error(res.message || 'Error'));
        } else {
            // 如果成功，直接返回后端响应结果中的 data 部分
            return res;
        }
    },
    error => {
        console.log('err' + error); // for debug
        ElMessage({
            message: error.message,
            type: 'error',
            duration: 5 * 1000
        });
        return Promise.reject(error);
    }
);

export default service;