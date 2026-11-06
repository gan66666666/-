package com.pet.adopt.service;

import com.pet.adopt.dto.RecordQueryDTO;
import com.pet.adopt.entity.AdoptRecord;
import com.pet.adopt.util.PageResult;

/**
 * 领养记录服务
 */
public interface AdoptRecordService {

    /**
     * 分页查询领养记录
     */
    PageResult<AdoptRecord> pageRecords(RecordQueryDTO query);

    /**
     * 查询领养记录详情
     */
    AdoptRecord getRecordById(Long id);
}
