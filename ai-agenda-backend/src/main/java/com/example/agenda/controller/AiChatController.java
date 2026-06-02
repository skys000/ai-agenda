package com.example.agenda.controller;

import com.example.agenda.result.Result;
import com.example.agenda.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
@CrossOrigin
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @GetMapping("/chat")
    public Result<String> chat(@RequestParam String msg, @RequestParam String sessionId) {
        try {
            String response = aiChatService.chat(msg, sessionId);
            return Result.success(response);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI 服务异常: " + e.getMessage());
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String msg, @RequestParam String sessionId) {
        try {
            return aiChatService.streamChat(msg, sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            return Flux.error(e);
        }
    }
}