package com.pet.adopt.service;

import com.pet.adopt.dto.VisitDTO;
import com.pet.adopt.dto.VisitQueryDTO;
import com.pet.adopt.entity.ReturnVisit;
import com.pet.adopt.util.PageResult;

/**
 * 回访服务
 */
public interface ReturnVisitService {

    /**
     * 分页查询回访记录
     */
    PageResult<ReturnVisit> pageVisits(VisitQueryDTO query);

    /**
     * 创建回访计划
     */
    void addVisit(VisitDTO visitDTO);

    /**
     * 登记回访结果
     */
    void updateVisit(VisitDTO visitDTO);

    /**
     * 删除回访记录
     */
    void deleteVisit(Long id);
}
