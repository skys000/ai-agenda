package com.example.agenda.service.function;

import com.example.agenda.pojo.entity.Schedule;
import com.example.agenda.service.CategoryService;
import com.example.agenda.service.ScheduleService;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@Slf4j
@Component
@Description("添加一个新的日程安排")
@RequiredArgsConstructor
public class AddScheduleFunction implements Function<AddScheduleFunction.Request, AddScheduleFunction.Response> {

    private final ScheduleService scheduleService;
    private final CategoryService categoryService;

    @Data
    @JsonClassDescription("添加日程的请求参数结构")
    public static class Request {
        @JsonPropertyDescription("当前操作用户的ID，请务必使用系统提示词(System Prompt)中提供的 'currentUserId' 值，严禁询问用户。")
        @JsonProperty(required = true)
        private Long userId;

        @JsonPropertyDescription("日程的标题，例如：开组会、去超市买菜")
        @JsonProperty(required = true)
        private String title;

        @JsonPropertyDescription("日程的详细内容或备注信息")
        private String description;

        @JsonPropertyDescription("开始时间，格式必须是：yyyy-MM-dd HH:mm:ss")
        @JsonProperty(required = true)
        private String startTime;

        @JsonPropertyDescription("结束时间，格式必须是：yyyy-MM-dd HH:mm:ss")
        private String endTime;

        @JsonPropertyDescription("日程优先级。请根据语气判断：1-低(普通), 2-中(默认), 3-高(紧急/重要)。")
        private Integer priority;

        @JsonPropertyDescription("日程分类名称。例如：'工作'、'学习'、'生活'。如果用户未明确提及，可为空。")
        private String categoryName;
    }

    @Data
    public static class Response {
        private String status;
        private String message;

        public Response(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }

    @Override
    public Response apply(Request request) {
        log.info("Add schedule by AI tool: userId={}, title={}, priority={}, category={}",
                request.userId, request.title, request.priority, request.categoryName);

        if (request.userId == null) {
            return new Response("error", "系统错误：未接收到用户ID");
        }

        try {
            Schedule schedule = new Schedule();

            schedule.setUserId(request.userId);
            schedule.setTitle(request.title);
            schedule.setDescription(request.description);

            LocalDateTime start = parseTime(request.startTime);
            schedule.setStartTime(start);
            if (request.endTime != null) {
                schedule.setEndTime(parseTime(request.endTime));
            } else {
                schedule.setEndTime(start.plusHours(1));
            }

            if (request.priority != null && request.priority >= 1 && request.priority <= 3) {
                schedule.setPriority(request.priority);
            } else {
                schedule.setPriority(2);
            }

            if (request.categoryName != null && !request.categoryName.isEmpty()) {
                Long categoryId = categoryService.getCategoryIdByName(request.userId, request.categoryName);
                if (categoryId != null) {
                    schedule.setCategoryId(categoryId);
                } else {
                    String originalDesc = schedule.getDescription() == null ? "" : schedule.getDescription();
                    schedule.setDescription(originalDesc + " (原分类: " + request.categoryName + ")");
                }
            }

            schedule.setStatus(0);
            schedule.setIsDeleted(0);

            scheduleService.save(schedule);

            return new Response("success", "日程已添加成功 (ID: " + schedule.getId() + ")");

        } catch (Exception e) {
            log.error("添加日程异常", e);
            return new Response("error", "保存失败: " + e.getMessage());
        }
    }

    private LocalDateTime parseTime(String timeStr) {
        try {
            String cleanTime = timeStr.replace("T", " ");
            if (cleanTime.length() == 16) cleanTime += ":00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(cleanTime, formatter);
        } catch (Exception e) {
            log.warn("Failed to parse schedule time: {}", timeStr);
            return LocalDateTime.now();
        }
    }
}
