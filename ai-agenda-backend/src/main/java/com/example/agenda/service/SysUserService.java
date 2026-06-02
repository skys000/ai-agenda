package com.example.agenda.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agenda.pojo.dto.AssignRoleDTO;
import com.example.agenda.pojo.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    /**
     * 为用户分配角色
     * @param assignRoleDTO 分配角色的数据传输对象
     */
    void assignRoles(AssignRoleDTO assignRoleDTO);

    /**
     * (新增) 用户登录认证
     * @param username 用户名
     * @param password 密码
     * @return 认证成功的用户信息
     */
    SysUser login(String username, String password);
}