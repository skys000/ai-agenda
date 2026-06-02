package com.example.agenda.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder // 加上 Builder 模式，方便后面快速构建对象
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_ai_message")
public class AiMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会话ID (对应 Redis 中的 chatId)
     */
    private String sessionId;

    /**
     * 角色 (user / assistant / system)
     */
    private String role;

    /**
     * 聊天内容 (设为 text 类型，防止内容过长)
     */
    private String content;

    /**
     * 发送时间
     */
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}