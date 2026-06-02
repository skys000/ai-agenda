package com.example.agenda.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedisChatMemory implements ChatMemory {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisChatMemory(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    private String getKey(String conversationId) {
        return "chat_memory:" + conversationId;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        String key = getKey(conversationId);
        try {
            // 1. 先取出旧的历史记录
            List<Message> history = this.get(conversationId, 100);

            // 2. 追加新消息
            history.addAll(messages);

            // 3. 截断，只保留最近 20 条（防止 Token 爆炸）
            if (history.size() > 20) {
                history = history.subList(history.size() - 20, history.size());
            }

            // 4. 【关键步骤】转成简单的 List<Map> 结构存储
            // 避免存储 Spring AI 的复杂对象，只存核心数据：role 和 content
            List<Map<String, String>> saveList = history.stream().map(msg -> {
                return Map.of(
                        "role", msg.getMessageType().getValue(),
                        "content", msg.getContent()
                );
            }).collect(Collectors.toList());

            // 5. 序列化为 JSON 字符串并存入 Redis
            String json = objectMapper.writeValueAsString(saveList);
            redisTemplate.opsForValue().set(key, json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = getKey(conversationId);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 1. 反序列化为 List<Map>
            List<Map<String, String>> loadList = objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});

            // 2. 【关键步骤】手动还原成 Spring AI 的 Message 对象
            List<Message> messages = new ArrayList<>();
            for (Map<String, String> map : loadList) {
                String role = map.get("role");
                String content = map.get("content");

                if ("USER".equalsIgnoreCase(role)) {
                    messages.add(new UserMessage(content));
                } else if ("ASSISTANT".equalsIgnoreCase(role)) {
                    messages.add(new AssistantMessage(content));
                } else if ("SYSTEM".equalsIgnoreCase(role)) {
                    messages.add(new SystemMessage(content));
                }
            }
            return messages;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(getKey(conversationId));
    }
}