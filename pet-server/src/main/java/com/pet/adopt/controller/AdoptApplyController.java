package com.pet.adopt.controller;

import com.pet.adopt.config.AuthInterceptor;
import com.pet.adopt.dto.ApplyDTO;
import com.pet.adopt.dto.ApplyQueryDTO;
import com.pet.adopt.dto.AuditDTO;
import com.pet.adopt.entity.AdoptApply;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.service.AdoptApplyService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 领养申请接口
 * 注意：server.servlet.context-path=/api，故对外完整路径为 /api/apply/**
 * 其中 POST /api/apply 需要 role=user 的令牌（用户端提交），其余接口需 role=admin 的令牌
 */
@RestController
@RequestMapping("/apply")
@RequiredArgsConstructor
public class AdoptApplyController {

    private final AdoptApplyService adoptApplyService;

    /**
     * 分页查询领养申请
     */
    @GetMapping("/page")
    public Result<PageResult<AdoptApply>> page(ApplyQueryDTO query) {
        return Result.success(adoptApplyService.pageApplies(query));
    }

    /**
     * 申请详情
     */
    @GetMapping("/{id}")
    public Result<AdoptApply> detail(@PathVariable("id") Long id) {
        return Result.success(adoptApplyService.getApplyById(id));
    }

    /**
     * 新增领养申请（用户端提交，需 role=user 令牌）
     */
    @PostMapping
    public Result<Void> add(@RequestBody ApplyDTO applyDTO, HttpServletRequest request) {
        adoptApplyService.addApply(applyDTO, getUserId(request));
        return Result.success("提交成功", null);
    }

    /**
     * 审核领养申请：通过则生成领养记录并把宠物置为已领养，驳回则保存驳回原因
     */
    @PutMapping("/audit")
    public Result<Void> audit(@RequestBody AuditDTO auditDTO) {
        adoptApplyService.auditApply(auditDTO);
        return Result.success("审核成功", null);
    }

    /**
     * 取拦截器写入的当前用户ID，取不到说明令牌缺失，视为未登录
     */
    private Long getUserId(HttpServletRequest request) {
        Object attr = request.getAttribute(AuthInterceptor.USER_ID_ATTRIBUTE);
        if (attr instanceof Long) {
            return (Long) attr;
        }
        throw new BusinessException(401, "未登录或登录已过期");
    }
}
