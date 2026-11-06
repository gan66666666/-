package com.pet.adopt.service;

import com.pet.adopt.dto.ApplyDTO;
import com.pet.adopt.dto.ApplyQueryDTO;
import com.pet.adopt.dto.AuditDTO;
import com.pet.adopt.dto.UserQueryDTO;
import com.pet.adopt.entity.AdoptApply;
import com.pet.adopt.util.PageResult;

/**
 * 领养申请服务
 */
public interface AdoptApplyService {

    /**
     * 分页查询领养申请
     */
    PageResult<AdoptApply> pageApplies(ApplyQueryDTO query);

    /**
     * 查询申请详情
     */
    AdoptApply getApplyById(Long id);

    /**
     * 新增领养申请（用户端提交，自动生成申请编号）
     */
    void addApply(ApplyDTO applyDTO, Long userId);

    /**
     * 分页查询当前登录用户自己的领养申请
     */
    PageResult<AdoptApply> pageMyApplies(Long userId, UserQueryDTO query);

    /**
     * 审核领养申请：通过则生成领养记录并把宠物置为已领养，驳回则记录驳回原因
     */
    void auditApply(AuditDTO auditDTO);
}
