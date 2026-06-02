package com.example.agenda.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_category")
public class Category implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 0代表公共分类，其他代表私有分类
    private String name;
    private String color; // 例如 #FF0000

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer isDeleted;
}