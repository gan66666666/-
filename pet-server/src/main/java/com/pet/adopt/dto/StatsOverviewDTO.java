package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台概览数据
 */
@Data
public class StatsOverviewDTO implements Serializable {

    /** 待审核申请数：adopt_apply.status = 0 */
    private Long pendingApply;

    /** 可领养宠物数：pet.status = 1 */
    private Long adoptablePet;

    /** 今日新增申请数：apply_time 在今天当天 */
    private Long todayApply;

    /** 本月成功领养数：adopt_date 在本月 */
    private Long monthAdopt;
}
