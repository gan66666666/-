package com.pet.adopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.adopt.dto.RecordQueryDTO;
import com.pet.adopt.entity.AdoptRecord;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.AdoptRecordMapper;
import com.pet.adopt.service.AdoptRecordService;
import com.pet.adopt.util.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 领养记录服务实现
 */
@Service
@RequiredArgsConstructor
public class AdoptRecordServiceImpl implements AdoptRecordService {

    private final AdoptRecordMapper adoptRecordMapper;

    @Override
    public PageResult<AdoptRecord> pageRecords(RecordQueryDTO query) {
        RecordQueryDTO safeQuery = query == null ? new RecordQueryDTO() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1
                ? 1L : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1
                ? 10L : safeQuery.getPageSize();

        LambdaQueryWrapper<AdoptRecord> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(safeQuery.getAdopterName())) {
            wrapper.like(AdoptRecord::getAdopterName, safeQuery.getAdopterName().trim());
        }
        if (safeQuery.getStartDate() != null) {
            wrapper.ge(AdoptRecord::getAdoptDate, safeQuery.getStartDate());
        }
        if (safeQuery.getEndDate() != null) {
            wrapper.le(AdoptRecord::getAdoptDate, safeQuery.getEndDate());
        }
        wrapper.orderByDesc(AdoptRecord::getAdoptDate).orderByDesc(AdoptRecord::getId);

        Page<AdoptRecord> page = adoptRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AdoptRecord> records = page.getRecords();
        return PageResult.of(page.getTotal(), records);
    }

    @Override
    public AdoptRecord getRecordById(Long id) {
        if (id == null) {
            throw new BusinessException("记录ID不能为空");
        }
        AdoptRecord record = adoptRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("领养记录不存在");
        }
        return record;
    }
}
