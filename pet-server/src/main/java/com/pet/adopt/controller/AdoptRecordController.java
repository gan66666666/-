package com.pet.adopt.controller;

import com.pet.adopt.dto.RecordQueryDTO;
import com.pet.adopt.entity.AdoptRecord;
import com.pet.adopt.service.AdoptRecordService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 领养记录接口
 * 注意：server.servlet.context-path=/api，故对外完整路径为 /api/record/**
 */
@RestController
@RequestMapping("/record")
@RequiredArgsConstructor
public class AdoptRecordController {

    private final AdoptRecordService adoptRecordService;

    /**
     * 分页查询领养记录
     */
    @GetMapping("/page")
    public Result<PageResult<AdoptRecord>> page(RecordQueryDTO query) {
        return Result.success(adoptRecordService.pageRecords(query));
    }

    /**
     * 领养记录详情
     */
    @GetMapping("/{id}")
    public Result<AdoptRecord> detail(@PathVariable("id") Long id) {
        return Result.success(adoptRecordService.getRecordById(id));
    }
}
