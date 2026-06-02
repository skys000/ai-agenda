package com.example.agenda.pojo.dto;

import lombok.Data;

@Data
public class ScheduleQueryDTO {
    private Long userId; // 后端设置
    private String title;
    private Long categoryId;
}