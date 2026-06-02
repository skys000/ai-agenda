package com.example.agenda.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ScheduleVO {
    private Long id;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    private Integer priority;
    private Integer status;
    private Long categoryId;

    // === 核心差异：这里存的是分类的名字，而不是 ID ===
    private String categoryName;
    private String categoryColor; // 顺便把颜色也查出来，前端显示标签颜色
}