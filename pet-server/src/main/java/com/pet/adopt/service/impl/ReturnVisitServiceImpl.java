package com.pet.adopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.adopt.dto.VisitDTO;
import com.pet.adopt.dto.VisitQueryDTO;
import com.pet.adopt.entity.ReturnVisit;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.ReturnVisitMapper;
import com.pet.adopt.service.ReturnVisitService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 回访服务实现
 */
@Service
@RequiredArgsConstructor
public class ReturnVisitServiceImpl implements ReturnVisitService {

    private final ReturnVisitMapper returnVisitMapper;

    @Override
    public PageResult<ReturnVisit> pageVisits(VisitQueryDTO query) {
        VisitQueryDTO safeQuery = query == null ? new VisitQueryDTO() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1
                ? 1L : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1
                ? 10L : safeQuery.getPageSize();

        LambdaQueryWrapper<ReturnVisit> wrapper = new LambdaQueryWrapper<>();
        if (safeQuery.getStatus() != null) {
            wrapper.eq(ReturnVisit::getStatus, safeQuery.getStatus());
        }
        // 排序规则：待回访(status=0)按计划回访时间升序排在最前，其余按创建顺序倒序
        // （return_visit 表无 create_time 字段，用自增 id 倒序近似“创建时间倒序”）
        wrapper.last("ORDER BY (status = 0) DESC, CASE WHEN status = 0 THEN plan_time END ASC, id DESC");

        Page<ReturnVisit> page = returnVisitMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ReturnVisit> records = page.getRecords();
        return PageResult.of(page.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addVisit(VisitDTO visitDTO) {
        if (visitDTO == null
                || !StringUtils.hasText(visitDTO.getPetName())
                || !StringUtils.hasText(visitDTO.getAdopterName())
                || visitDTO.getPlanTime() == null) {
            throw new BusinessException("宠物名称、领养人和计划回访时间不能为空");
        }
        // 与 return_visit 表的列宽一致
        ValidationUtil.maxLength(visitDTO.getPetName(), 50, "宠物名称");
        ValidationUtil.maxLength(visitDTO.getAdopterName(), 50, "领养人姓名");
        ValidationUtil.maxLength(visitDTO.getContent(), 500, "回访内容");

        ReturnVisit visit = new ReturnVisit();
        // 新增场景忽略前端传入的 id，主键一律交由数据库自增生成，避免主键冲突与数据错乱
        visit.setId(null);
        visit.setPetName(visitDTO.getPetName().trim());
        visit.setAdopterName(visitDTO.getAdopterName().trim());
        visit.setPlanTime(visitDTO.getPlanTime());
        visit.setContent(visitDTO.getContent());
        // 新建回访计划默认待回访
        visit.setStatus(visitDTO.getStatus() == null ? 0 : visitDTO.getStatus());
        returnVisitMapper.insert(visit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVisit(VisitDTO visitDTO) {
        if (visitDTO == null || visitDTO.getId() == null) {
            throw new BusinessException("回访ID不能为空");
        }
        ReturnVisit exist = returnVisitMapper.selectById(visitDTO.getId());
        if (exist == null) {
            throw new BusinessException("回访记录不存在");
        }
        ValidationUtil.maxLength(visitDTO.getPetName(), 50, "宠物名称");
        ValidationUtil.maxLength(visitDTO.getAdopterName(), 50, "领养人姓名");
        ValidationUtil.maxLength(visitDTO.getContent(), 500, "回访内容");

        ReturnVisit visit = new ReturnVisit();
        visit.setId(visitDTO.getId());
        if (StringUtils.hasText(visitDTO.getPetName())) {
            visit.setPetName(visitDTO.getPetName().trim());
        }
        if (StringUtils.hasText(visitDTO.getAdopterName())) {
            visit.setAdopterName(visitDTO.getAdopterName().trim());
        }
        if (visitDTO.getPlanTime() != null) {
            visit.setPlanTime(visitDTO.getPlanTime());
        }
        if (visitDTO.getContent() != null) {
            visit.setContent(visitDTO.getContent());
        }
        // 登记回访结果后置为已完成
        visit.setStatus(1);
        returnVisitMapper.updateById(visit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVisit(Long id) {
        if (id == null) {
            throw new BusinessException("回访ID不能为空");
        }
        if (returnVisitMapper.selectById(id) == null) {
            throw new BusinessException("回访记录不存在");
        }
        returnVisitMapper.deleteById(id);
    }
}
