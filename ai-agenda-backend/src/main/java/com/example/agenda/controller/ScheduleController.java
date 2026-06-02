package com.example.agenda.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agenda.mapper.ScheduleMapper;
import com.example.agenda.pojo.dto.ScheduleQueryDTO;
import com.example.agenda.pojo.entity.Schedule;
import com.example.agenda.pojo.vo.ScheduleVO;
import com.example.agenda.result.Result;
import com.example.agenda.service.ScheduleService;
import com.example.agenda.util.BaseContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "日程管理") // Knife4j 分组名称
@RestController
@RequestMapping("/schedule")
@CrossOrigin // 允许跨域，方便调试
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired private ScheduleMapper scheduleMapper;
    @Operation(summary = "新增日程")
    @PostMapping("/add")
    public Result<String> add(@RequestBody Schedule schedule) {
        // 获取当前用户ID
        schedule.setUserId(BaseContext.getCurrentId());

        // 简单补全时间，防止空指针
        if (schedule.getCreateTime() == null) {
            schedule.setCreateTime(LocalDateTime.now());
            schedule.setUpdateTime(LocalDateTime.now());
        }

        scheduleService.save(schedule);
        return Result.success("添加成功");
    }

    @Operation(summary = "修改日程")
    @PutMapping("/update")
    public Result<String> update(@RequestBody Schedule schedule) {
        schedule.setUpdateTime(LocalDateTime.now());
        scheduleService.updateById(schedule);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除日程")
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        scheduleService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "分页查询列表(VO)")
    @GetMapping("/list")
    public Result<Page<ScheduleVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            // 接收更复杂的查询参数
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId) {

        Page<ScheduleVO> page = new Page<>(pageNum, pageSize);
        ScheduleQueryDTO queryDTO = new ScheduleQueryDTO();
        queryDTO.setUserId(BaseContext.getCurrentId()); // 获取当前用户
        queryDTO.setTitle(title);
        queryDTO.setCategoryId(categoryId);

        // 调用我们手写的联查方法
        return Result.success(scheduleMapper.selectVoPage(page, queryDTO));
    }
}