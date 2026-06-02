package com.example.agenda.config;

import com.example.agenda.service.CategoryService;
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

    @Bean("addSchedule")
    @Description("添加一个新的日程安排")
    public Function<AddScheduleFunction.Request, AddScheduleFunction.Response> addSchedule(
            ScheduleService scheduleService,
            CategoryService categoryService) {
        return new AddScheduleFunction(scheduleService, categoryService);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultFunctions("addSchedule")
                .build();
    }
}
