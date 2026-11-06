package com.pet.adopt.controller;

import com.pet.adopt.dto.StatsOverviewDTO;
import com.pet.adopt.dto.StatsPetTypeDTO;
import com.pet.adopt.dto.StatsTrendDTO;
import com.pet.adopt.service.StatsService;
import com.pet.adopt.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工作台统计接口
 * 注意：server.servlet.context-path=/api，故对外完整路径为 /api/stats/**
 * 全部接口都需要登录 token（登录拦截器对 /api/** 生效）
 */
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 工作台概览：4 个数据卡片的值
     */
    @GetMapping("/overview")
    public Result<StatsOverviewDTO> overview() {
        return Result.success(statsService.getOverview());
    }

    /**
     * 领养趋势：week 近7天、month 近30天、year 近12个月
     */
    @GetMapping("/trend")
    public Result<List<StatsTrendDTO>> trend(@RequestParam(value = "type", required = false, defaultValue = "week") String type) {
        return Result.success(statsService.getTrend(type));
    }

    /**
     * 宠物类型分布（用于饼图）
     */
    @GetMapping("/petType")
    public Result<List<StatsPetTypeDTO>> petType() {
        return Result.success(statsService.getPetTypeDistribution());
    }
}
