package com.example.agenda.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.agenda.mapper.ScheduleMapper;
import com.example.agenda.pojo.entity.Schedule;
import com.example.agenda.service.ScheduleService;
import org.springframework.stereotype.Service;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
    // 继承 ServiceImpl 后，save, update, remove, getById 等方法直接可用
}