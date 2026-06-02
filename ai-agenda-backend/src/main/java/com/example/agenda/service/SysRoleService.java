package com.example.agenda.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agenda.pojo.dto.AssignMenuDTO;
import com.example.agenda.pojo.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {
    // 增加授权方法
    void assignMenus(AssignMenuDTO assignMenuDTO);
}