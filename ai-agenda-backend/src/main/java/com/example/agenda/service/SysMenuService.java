// SysMenuService.java (修改后)
package com.example.agenda.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.agenda.pojo.entity.SysMenu;
import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID构建其有权访问的菜单树
     * @param userId 用户ID
     * @return 菜单树列表
     */
    List<SysMenu> buildTreeForUser(Long userId);
}