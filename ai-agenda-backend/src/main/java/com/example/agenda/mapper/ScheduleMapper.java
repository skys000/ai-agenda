package com.example.agenda.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.agenda.pojo.dto.ScheduleQueryDTO;
import com.example.agenda.pojo.entity.Schedule;
import com.example.agenda.pojo.vo.ScheduleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 日程表 Mapper 接口
 * 继承 BaseMapper 后，自动拥有 CRUD 能力
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    // 删除这里的 @Select 注解
    Page<ScheduleVO> selectVoPage(Page<ScheduleVO> page, @Param("query") ScheduleQueryDTO query);

}