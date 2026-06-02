package com.example.agenda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.agenda.pojo.entity.AiMessage;
import com.example.agenda.result.Result;
import com.example.agenda.service.AiMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 引入Page
import org.springframework.util.StringUtils; // 引入StringUtils
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper; // 引入 QueryWrapper

@Tag(name = "AI历史记录")
@RestController
@RequestMapping("/ai-message")
@CrossOrigin
public class AiMessageController {

    @Autowired
    private AiMessageService aiMessageService;

    @Operation(summary = "查询某个会话的历史记录")
    @GetMapping("/list/{sessionId}")
    public Result<List<AiMessage>> getHistory(@PathVariable String sessionId) {
        LambdaQueryWrapper<AiMessage> query = new LambdaQueryWrapper<>();
        query.eq(AiMessage::getSessionId, sessionId);
        query.orderByAsc(AiMessage::getCreateTime); // 按时间正序，还原聊天场景

        return Result.success(aiMessageService.list(query));
    }
    @Operation(summary = "分页查询所有AI消息")
    @GetMapping("/list")
    public Result<Page<AiMessage>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String sessionId,
            // 新增排序参数
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {

        Page<AiMessage> page = new Page<>(pageNum, pageSize);
        QueryWrapper<AiMessage> query = new QueryWrapper<>();

        if (StringUtils.hasText(sessionId)) {
            query.lambda().like(AiMessage::getSessionId, sessionId);
        }

        // 【核心逻辑】处理排序
        if (StringUtils.hasText(sortField)) {
            // 将前端传来的驼峰命名（如 createTime）转为数据库的下划线命名（create_time）
            String dbField = com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(sortField);
            if ("asc".equalsIgnoreCase(sortOrder)) {
                query.orderByAsc(dbField);
            } else {
                query.orderByDesc(dbField);
            }
        } else {
            // 默认排序
            query.lambda().orderByDesc(AiMessage::getCreateTime);
        }

        return Result.success(aiMessageService.page(page, query));
    }
}