package com.example.agenda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agenda.pojo.dto.AssignRoleDTO;
import com.example.agenda.pojo.dto.LoginDTO; // 新增导入
import com.example.agenda.pojo.entity.SysUser;
import com.example.agenda.pojo.entity.SysUserRole;
import com.example.agenda.pojo.vo.LoginVO; // 新增导入
import com.example.agenda.result.Result;
import com.example.agenda.service.SysUserRoleService;
import com.example.agenda.service.SysUserService;
import com.example.agenda.util.JwtUtil; // 新增导入
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "系统用户管理")
@RestController
@RequestMapping("/sys-user")
@CrossOrigin // 注意：Spring Security接管后，这个注解可能失效，建议使用Security的CORS配置
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    // 新增注入 JwtUtil
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //============== 新增登录接口 ==============
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // 1. 调用Service进行用户认证
        SysUser user = sysUserService.login(loginDTO.getUsername(), loginDTO.getPassword());

        // 2. 认证成功，使用JwtUtil生成JWT
        String token = jwtUtil.generateToken(user);

        // 3. 封装VO并返回
        return Result.success(new LoginVO(token));
    }
    //========================================

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    public Result<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String username) {

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            query.like(SysUser::getUsername, username);
        }
        query.orderByDesc(SysUser::getCreateTime);

        return Result.success(sysUserService.page(page, query));
    }

    @Operation(summary = "新增用户")
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysUser sysUser) {
        if (!StringUtils.hasText(sysUser.getPassword())) {
            return Result.error("密码不能为空");
        }
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        sysUserService.save(sysUser);
        return Result.success("新增成功");
    }

    @Operation(summary = "修改用户")
    @PutMapping("/update")
    public Result<String> update(@RequestBody SysUser sysUser) {
        if (StringUtils.hasText(sysUser.getPassword())) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        } else {
            sysUser.setPassword(null);
        }
        sysUserService.updateById(sysUser);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "为用户分配角色")
    @PostMapping("/assign-roles")
    public Result<String> assignRoles(@RequestBody AssignRoleDTO assignRoleDTO) {
        sysUserService.assignRoles(assignRoleDTO);
        return Result.success("角色分配成功");
    }

    @Operation(summary = "查询指定用户拥有的角色ID列表")
    @GetMapping("/assigned-roles/{userId}")
    public Result<List<Long>> getAssignedRoles(@PathVariable Long userId) {
        List<SysUserRole> assignedList = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
        );

        List<Long> roleIds = assignedList.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        return Result.success(roleIds);
    }
}
