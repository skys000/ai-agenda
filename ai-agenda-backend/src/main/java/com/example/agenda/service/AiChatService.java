package com.example.agenda.service;

import reactor.core.publisher.Flux;

public interface AiChatService {
    // 普通对话
    String chat(String msg, String sessionId);
    // 流式对话
    Flux<String> streamChat(String msg, String sessionId);
}