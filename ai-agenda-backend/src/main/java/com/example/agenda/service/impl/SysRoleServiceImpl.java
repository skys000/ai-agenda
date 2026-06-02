package com.example.agenda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agenda.mapper.SysRoleMapper;
import com.example.agenda.pojo.dto.AssignMenuDTO;
import com.example.agenda.pojo.entity.SysRole;
import com.example.agenda.pojo.entity.SysRoleMenu;
import com.example.agenda.service.SysRoleMenuService;
import com.example.agenda.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Override
    @Transactional // 开启事务，保证操作的原子性
    public void assignMenus(AssignMenuDTO assignMenuDTO) {
        // 1. 先删除该角色之前所有的权限
        sysRoleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, assignMenuDTO.getRoleId()));

        // 2. 再批量插入新的权限
        List<SysRoleMenu> roleMenus = assignMenuDTO.getMenuIds().stream()
                .map(menuId -> new SysRoleMenu(assignMenuDTO.getRoleId(), menuId))
                .collect(Collectors.toList());

        if (!roleMenus.isEmpty()) {
            sysRoleMenuService.saveBatch(roleMenus);
        }
    }
}