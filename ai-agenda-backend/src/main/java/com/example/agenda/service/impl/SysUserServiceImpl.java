package com.example.agenda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agenda.mapper.SysUserMapper;
import com.example.agenda.pojo.dto.AssignRoleDTO;
import com.example.agenda.pojo.entity.SysUser;
import com.example.agenda.pojo.entity.SysUserRole;
import com.example.agenda.service.SysUserRoleService;
import com.example.agenda.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void assignRoles(AssignRoleDTO assignRoleDTO) {
        // 1. 先删除该用户之前的所有角色
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, assignRoleDTO.getUserId()));

        // 2. 如果角色列表不为空，则批量插入新角色
        if (assignRoleDTO.getRoleIds() != null && !assignRoleDTO.getRoleIds().isEmpty()) {
            List<SysUserRole> userRoles = assignRoleDTO.getRoleIds().stream()
                    .map(roleId -> new SysUserRole(assignRoleDTO.getUserId(), roleId))
                    .collect(Collectors.toList());
            sysUserRoleService.saveBatch(userRoles);
        }
    }

    //============== 新增登录逻辑 ==============
    @Override
    public SysUser login(String username, String password) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = this.getOne(wrapper);

        // 2. 判断用户是否存在 (为防止时序攻击，不明确提示是用户名还是密码错误)
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 认证成功, 返回用户信息
        return user;
    }
    //========================================
}
