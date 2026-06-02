import request from '@/utils/request';

// 获取日程列表
export const getScheduleList = (params) => {
    return request({
        url: '/schedule/list',
        method: 'get',
        params
    });
}

// 获取分类列表
export const getCategoryList = () => {
    return request({
        url: '/category/list',
        method: 'get'
    });
}

// 新增日程
export const addSchedule = (data) => {
    return request({
        url: '/schedule/add',
        method: 'post',
        data
    });
}

// 修改日程
export const updateSchedule = (data) => {
    return request({
        url: '/schedule/update',
        method: 'put',
        data
    });
}

// 删除日程
export const deleteSchedule = (id) => {
    return request({
        url: `/schedule/delete/${id}`,
        method: 'delete'
    });
}
