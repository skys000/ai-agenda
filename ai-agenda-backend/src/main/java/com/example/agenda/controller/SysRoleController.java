package com.example.agenda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agenda.pojo.dto.AssignMenuDTO;
import com.example.agenda.pojo.entity.SysRole;
import com.example.agenda.pojo.entity.SysRoleMenu;
import com.example.agenda.result.Result;
import com.example.agenda.service.SysRoleMenuService;
import com.example.agenda.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "系统角色管理")
@RestController
@RequestMapping("/sys-role")
@CrossOrigin
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Operation(summary = "查询所有角色列表")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(sysRoleService.list());
    }

    @Operation(summary = "新增角色")
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysRole sysRole) {
        sysRoleService.save(sysRole);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改角色")
    @PutMapping("/update")
    public Result<String> update(@RequestBody SysRole sysRole) {
        sysRoleService.updateById(sysRole);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 实际开发中要判断角色是否被用户关联
        sysRoleService.removeById(id);
        return Result.success("删除成功");
    }
    @Operation(summary = "为角色分配菜单权限")
    @PostMapping("/assign-menus")
    public Result<String> assignMenus(@RequestBody AssignMenuDTO assignMenuDTO) {
        sysRoleService.assignMenus(assignMenuDTO);
        return Result.success("权限分配成功");
    }
    @Operation(summary = "查询指定角色拥有的菜单ID列表")
    @GetMapping("/assigned-menus/{roleId}")
    public Result<List<Long>> getAssignedMenus(@PathVariable Long roleId) {
        // 1. 查询关联表，找到所有 roleId 匹配的记录
        List<SysRoleMenu> assignedList = sysRoleMenuService.list(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );

        // 2. 从结果中提取出 menuId，并返回一个纯粹的 ID 列表
        List<Long> menuIds = assignedList.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());

        return Result.success(menuIds);
    }
}