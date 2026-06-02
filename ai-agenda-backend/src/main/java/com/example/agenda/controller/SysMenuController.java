// SysMenuController.java (修改后)
package com.example.agenda.controller;

import com.example.agenda.pojo.entity.SysMenu;
import com.example.agenda.result.Result;
import com.example.agenda.service.SysMenuService;
import com.example.agenda.util.BaseContext; // 引入 BaseContext
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统菜单管理")
@RestController
@RequestMapping("/sys-menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @Operation(summary = "获取当前用户的菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        // 1. 从 ThreadLocal 获取当前登录用户的ID
        Long userId = BaseContext.getCurrentId();

        // 健壮性判断
        if (userId == null) {
            return Result.error("用户未登录或Token无效");
        }

        // 2. 调用 Service 层，传入 userId 来构建专属的菜单树
        List<SysMenu> menuTree = sysMenuService.buildTreeForUser(userId);

        return Result.success(menuTree);
    }

    // ... 增删改接口保持不变 ...
    @Operation(summary = "新增菜单")
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysMenu sysMenu) {
        sysMenuService.save(sysMenu);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改菜单")
    @PutMapping("/update")
    public Result<String> update(@RequestBody SysMenu sysMenu) {
        sysMenuService.updateById(sysMenu);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        sysMenuService.removeById(id);
        return Result.success("删除成功");
    }
}