package com.pet.adopt.service;

import com.pet.adopt.dto.StatsOverviewDTO;
import com.pet.adopt.dto.StatsPetTypeDTO;
import com.pet.adopt.dto.StatsTrendDTO;

import java.util.List;

/**
 * 工作台统计服务
 */
public interface StatsService {

    /**
     * 工作台概览：待审核申请数、可领养宠物数、今日新增申请数、本月成功领养数
     */
    StatsOverviewDTO getOverview();

    /**
     * 领养趋势数据
     *
     * @param type 统计维度：week 近7天、month 近30天、year 近12个月
     */
    List<StatsTrendDTO> getTrend(String type);

    /**
     * 宠物类型分布（用于饼图）
     */
    List<StatsPetTypeDTO> getPetTypeDistribution();
}
