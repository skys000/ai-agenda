package com.example.agenda.service.impl;

import com.example.agenda.pojo.entity.AiMessage;
import com.example.agenda.service.AiChatService;
import com.example.agenda.service.AiMessageService;
import com.example.agenda.util.BaseContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final AiMessageService aiMessageService;

    // 注入我们刚才创建的模板文件
    @Value("classpath:prompts/system.st")
    private Resource systemPromptResource;

    /**
     * 构建动态系统提示词（核心地基）
     */
    private String buildSystemPrompt(Long userId) {
        // 1. 加载模板
        SystemPromptTemplate template = new SystemPromptTemplate(systemPromptResource);

        // 2. 填充变量 (userId 和 时间)
        // 以后如果用了数据库，就在这里查库，把查出来的提示词字符串传给 template 即可
        return template.createMessage(Map.of(
                "currentUserId", userId,
                "currentTime", LocalDateTime.now().toString()
        )).getContent();
    }

    @Override
    public String chat(String msg, String sessionId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) throw new RuntimeException("用户未认证");

        // 1. 存用户消息
        saveMessage(userId, sessionId, "user", msg);

        // 2. 生成动态 Prompt
        String systemText = buildSystemPrompt(userId);

        // 3. 调用 AI
        String response = chatClient.prompt()
                .system(systemText) // 【注入】这里覆盖了 Config 里的默认配置
                .user(msg)
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .functions("addSchedule") // 以后这里可以从数据库读 List<String>
                .call()
                .content();

        // 4. 存 AI 消息
        saveMessage(userId, sessionId, "assistant", response);

        return response;
    }

    @Override
    public Flux<String> streamChat(String msg, String sessionId) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) return Flux.error(new RuntimeException("用户未认证"));

        saveMessage(userId, sessionId, "user", msg);

        String systemText = buildSystemPrompt(userId);
        StringBuilder fullContent = new StringBuilder();

        return chatClient.prompt()
                .system(systemText) // 【注入】
                .user(msg)
                .advisors(new MessageChatMemoryAdvisor(chatMemory))
                .advisors(a -> a.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .functions("addSchedule")
                .stream()
                .content()
                .doOnNext(fullContent::append)
                .doOnComplete(() -> {
                    // 流结束，存库
                    saveMessage(userId, sessionId, "assistant", fullContent.toString());
                });
    }

    private void saveMessage(Long userId, String sessionId, String role, String content) {
        aiMessageService.save(AiMessage.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createTime(LocalDateTime.now())
                .build());
    }
}