package com.example.agenda.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("sys_user_role")
@AllArgsConstructor // for new SysUserRole(userId, roleId)
@NoArgsConstructor  // for MyBatis-Plus
public class SysUserRole {
    // 假设你的关联表没有自增 id，只有 userId 和 roleId
    private Long userId;
    private Long roleId;
}