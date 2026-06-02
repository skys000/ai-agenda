// SysMenuServiceImpl.java (修改后)
package com.example.agenda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agenda.mapper.SysMenuMapper;
import com.example.agenda.pojo.entity.SysMenu;
import com.example.agenda.pojo.entity.SysRoleMenu;
import com.example.agenda.pojo.entity.SysUserRole;
import com.example.agenda.service.SysMenuService;
import com.example.agenda.service.SysRoleMenuService; // 引入
import com.example.agenda.service.SysUserRoleService; // 引入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    // 注入关联服务
    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> buildTreeForUser(Long userId) {
        // 1. 根据 userId 查询用户拥有的所有 roleId
        List<SysUserRole> userRoles = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));

        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        // 2. 根据 roleIds 查询这些角色被授权的所有 menuId (去重)
        List<SysRoleMenu> roleMenus = sysRoleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds));

        if (roleMenus.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());

        // 如果没有任何菜单权限，也直接返回
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 根据 menuIds 查询所有的菜单实体，并按 orderNum 排序
        List<SysMenu> userMenus = this.list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .orderByAsc(SysMenu::getOrderNum));

        // 4. 将该用户有权限的、扁平的菜单列表构建成树形结构
        return buildMenuTree(userMenus);
    }

    /**
     * 私有辅助方法：将菜单列表构建成树形结构
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menuList) {
        List<SysMenu> finalTree = new ArrayList<>();
        // 使用 Map 优化查找，避免在循环中嵌套循环，提高性能
        java.util.Map<Long, SysMenu> menuMap = menuList.stream()
                .collect(Collectors.toMap(SysMenu::getId, menu -> menu));

        for (SysMenu menu : menuList) {
            if (menu.getParentId() == 0L) {
                finalTree.add(menu);
            } else {
                SysMenu parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(menu);
                }
            }
        }
        
        // 对根节点和所有子节点进行排序，确保每一层都按照 orderNum 排序
        finalTree.sort(Comparator.comparing(SysMenu::getOrderNum));
        sortChildrenRecursively(finalTree);
        
        return finalTree;
    }
    
    /**
     * 递归对子节点进行排序
     */
    private void sortChildrenRecursively(List<SysMenu> menus) {
        for (SysMenu menu : menus) {
            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                // 对当前层级的子节点进行排序
                menu.getChildren().sort(Comparator.comparing(SysMenu::getOrderNum));
                // 递归对下一层级进行排序
                sortChildrenRecursively(menu.getChildren());
            }
        }
    }
}