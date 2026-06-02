package com.example.agenda.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@TableName("sys_role_menu")
@AllArgsConstructor
public class SysRoleMenu {
    private Long roleId;
    private Long menuId;
}