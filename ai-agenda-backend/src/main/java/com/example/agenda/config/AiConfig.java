package com.example.agenda.config;

import com.example.agenda.service.CategoryService; // 【新增导入】
import com.example.agenda.service.ScheduleService;
import com.example.agenda.service.function.AddScheduleFunction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.function.Function;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory(RedisTemplate<String, String> redisTemplate) {
        return new RedisChatMemory(redisTemplate);
    }

    /**
     * 注册 Function 工具
     * 【关键修改】这里增加了 CategoryService 参数
     */
    @Bean("addSchedule")
    @Description("添加一个新的日程安排")
    public Function<AddScheduleFunction.Request, AddScheduleFunction.Response> addSchedule(
            ScheduleService scheduleService,
            CategoryService categoryService) { // 1. 注入 CategoryService

        // 2. 传入构造函数 (因为 AddScheduleFunction 现在需要两个 Service)
        return new AddScheduleFunction(scheduleService, categoryService);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                // .defaultSystem(...) // 你的注释是对的，这里去掉了，改为在 Service 层动态生成
                .defaultFunctions("addSchedule")
                .build();
    }
}