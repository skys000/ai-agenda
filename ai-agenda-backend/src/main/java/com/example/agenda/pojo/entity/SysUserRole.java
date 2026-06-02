package com.example.agenda.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("sys_user_role")
@AllArgsConstructor
@NoArgsConstructor
public class SysUserRole {
    private Long userId;
    private Long roleId;
}
