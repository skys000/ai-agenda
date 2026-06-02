package com.example.agenda.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agenda.mapper.SysUserRoleMapper;
import com.example.agenda.pojo.entity.SysUserRole;
import com.example.agenda.service.SysUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
}