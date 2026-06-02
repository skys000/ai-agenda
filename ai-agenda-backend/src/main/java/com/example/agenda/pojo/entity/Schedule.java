package com.example.agenda.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日程安排实体类
 * 对应数据库表: t_schedule
 */
@Data
@TableName("t_schedule")
public class Schedule implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 分类ID (关联 t_category)
     */
    private Long categoryId;

    /**
     * 日程标题
     */
    private String title;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 开始时间
     * JsonFormat 用于前端传 JSON 时间字符串时自动转成 Java 对象
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 优先级 (1-低, 2-中, 3-高)
     */
    private Integer priority;

    /**
     * 状态 (0-未完成, 1-已完成, 2-已取消)
     */
    private Integer status;

    /**
     * 创建时间 (MyBatis Plus 自动填充需配置，这里暂时手动设或依靠数据库默认值)
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除 (0-未删, 1-已删)
     */
    @TableLogic
    private Integer isDeleted;
}