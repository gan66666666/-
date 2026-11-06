package com.pet.adopt.controller;

import com.pet.adopt.dto.VisitDTO;
import com.pet.adopt.dto.VisitQueryDTO;
import com.pet.adopt.entity.ReturnVisit;
import com.pet.adopt.service.ReturnVisitService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回访接口
 * 注意：server.servlet.context-path=/api，故对外完整路径为 /api/visit/**
 */
@RestController
@RequestMapping("/visit")
@RequiredArgsConstructor
public class ReturnVisitController {

    private final ReturnVisitService returnVisitService;

    /**
     * 分页查询回访记录
     */
    @GetMapping("/page")
    public Result<PageResult<ReturnVisit>> page(VisitQueryDTO query) {
        return Result.success(returnVisitService.pageVisits(query));
    }

    /**
     * 创建回访计划
     */
    @PostMapping
    public Result<Void> add(@RequestBody VisitDTO visitDTO) {
        returnVisitService.addVisit(visitDTO);
        return Result.success("新增成功", null);
    }

    /**
     * 登记回访结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody VisitDTO visitDTO) {
        returnVisitService.updateVisit(visitDTO);
        return Result.success("修改成功", null);
    }

    /**
     * 删除回访记录
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        returnVisitService.deleteVisit(id);
        return Result.success("删除成功", null);
    }
}
