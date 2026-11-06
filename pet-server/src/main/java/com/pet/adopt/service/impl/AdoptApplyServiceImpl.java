package com.pet.adopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.adopt.dto.ApplyDTO;
import com.pet.adopt.dto.ApplyQueryDTO;
import com.pet.adopt.dto.AuditDTO;
import com.pet.adopt.dto.UserQueryDTO;
import com.pet.adopt.entity.AdoptApply;
import com.pet.adopt.entity.AdoptRecord;
import com.pet.adopt.entity.Pet;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.AdoptApplyMapper;
import com.pet.adopt.mapper.AdoptRecordMapper;
import com.pet.adopt.mapper.PetMapper;
import com.pet.adopt.service.AdoptApplyService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 领养申请服务实现
 */
@Service
@RequiredArgsConstructor
public class AdoptApplyServiceImpl implements AdoptApplyService {

    /** 申请编号格式：AP + yyyyMMddHHmmss + 4位随机数 */
    private static final DateTimeFormatter APPLY_NO_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AdoptApplyMapper adoptApplyMapper;

    private final AdoptRecordMapper adoptRecordMapper;

    private final PetMapper petMapper;

    @Override
    public PageResult<AdoptApply> pageApplies(ApplyQueryDTO query) {
        ApplyQueryDTO safeQuery = query == null ? new ApplyQueryDTO() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1
                ? 1L : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1
                ? 10L : safeQuery.getPageSize();

        LambdaQueryWrapper<AdoptApply> wrapper = new LambdaQueryWrapper<>();
        if (safeQuery.getStatus() != null) {
            wrapper.eq(AdoptApply::getStatus, safeQuery.getStatus());
        }
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like(AdoptApply::getApplicantName, keyword)
                    .or().like(AdoptApply::getApplyNo, keyword));
        }
        wrapper.orderByDesc(AdoptApply::getApplyTime).orderByDesc(AdoptApply::getId);

        Page<AdoptApply> page = adoptApplyMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AdoptApply> records = page.getRecords();
        return PageResult.of(page.getTotal(), records);
    }

    @Override
    public AdoptApply getApplyById(Long id) {
        if (id == null) {
            throw new BusinessException("申请ID不能为空");
        }
        AdoptApply apply = adoptApplyMapper.selectById(id);
        if (apply == null) {
            throw new BusinessException("领养申请不存在");
        }
        return apply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addApply(ApplyDTO applyDTO, Long userId) {
        if (applyDTO == null
                || !StringUtils.hasText(applyDTO.getApplicantName())
                || !StringUtils.hasText(applyDTO.getPhone())
                || applyDTO.getPetId() == null) {
            throw new BusinessException("申请人姓名、联系电话和宠物ID不能为空");
        }
        // 与 adopt_apply 表的列宽一致，避免超长文本落库时报 500
        ValidationUtil.maxLength(applyDTO.getApplicantName(), 50, "申请人姓名");
        ValidationUtil.maxLength(applyDTO.getPhone(), 20, "联系电话");
        ValidationUtil.maxLength(applyDTO.getPetName(), 50, "宠物名字");
        ValidationUtil.maxLength(applyDTO.getReason(), 500, "领养理由");

        // 校验目标宠物：必须存在，且处于可领养状态（status=1）。
        // 用户端前端已对「已领养 / 救助中」的宠物置灰申请按钮，但接口侧同样必须拦截，
        // 否则管理员一旦审核通过，就会出现同一只宠物被重复领养、生成两条领养记录的脏数据。
        Pet pet = petMapper.selectById(applyDTO.getPetId());
        if (pet == null) {
            throw new BusinessException("申请领养的宠物不存在");
        }
        if (pet.getStatus() == null || pet.getStatus() != 1) {
            throw new BusinessException("该宠物已被领养或暂不可领养，无法提交申请");
        }

        // 同一用户对同一宠物已有待审核申请时不再重复受理（驳回后允许重新提交）
        if (userId != null) {
            Long pendingCount = adoptApplyMapper.selectCount(
                    new LambdaQueryWrapper<AdoptApply>()
                            .eq(AdoptApply::getUserId, userId)
                            .eq(AdoptApply::getPetId, applyDTO.getPetId())
                            .eq(AdoptApply::getStatus, 0));
            if (pendingCount != null && pendingCount > 0) {
                throw new BusinessException("您已提交过该宠物的领养申请，请等待审核");
            }
        }

        AdoptApply apply = new AdoptApply();
        // 新增场景忽略前端传入的 id，主键一律交由数据库自增生成，避免主键冲突与数据错乱
        apply.setId(null);
        // 提交人取自登录令牌中的用户ID，供「我的申请」按用户过滤
        apply.setUserId(userId);
        apply.setApplyNo(generateApplyNo());
        apply.setApplicantName(applyDTO.getApplicantName().trim());
        apply.setPhone(applyDTO.getPhone().trim());
        apply.setPetId(applyDTO.getPetId());
        // 宠物名字：优先取入参，未传则用刚查出的宠物档案里的名字
        apply.setPetName(StringUtils.hasText(applyDTO.getPetName())
                ? applyDTO.getPetName().trim()
                : pet.getName());
        apply.setReason(applyDTO.getReason());
        // 新申请默认待审核
        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        adoptApplyMapper.insert(apply);
    }

    @Override
    public PageResult<AdoptApply> pageMyApplies(Long userId, UserQueryDTO query) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        UserQueryDTO safeQuery = query == null ? new UserQueryDTO() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1
                ? 1L : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1
                ? 10L : safeQuery.getPageSize();

        LambdaQueryWrapper<AdoptApply> wrapper = new LambdaQueryWrapper<>();
        // 只查当前登录用户自己提交的申请
        wrapper.eq(AdoptApply::getUserId, userId)
                .orderByDesc(AdoptApply::getApplyTime)
                .orderByDesc(AdoptApply::getId);

        Page<AdoptApply> page = adoptApplyMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getRecords());
    }

    /**
     * 审核申请流转：
     * status=1 通过 → 申请状态置为通过 → 自动生成领养记录 → 对应宠物状态置为 2 已领养；
     * status=2 驳回 → 申请状态置为驳回并保存驳回原因。
     * 方法带事务，任一步失败整体回滚，避免出现“申请已通过但没有领养记录”的脏数据。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(AuditDTO auditDTO) {
        if (auditDTO == null || auditDTO.getId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (auditDTO.getStatus() == null) {
            throw new BusinessException("审核状态不能为空");
        }
        ValidationUtil.maxLength(auditDTO.getRejectReason(), 200, "驳回原因");
        AdoptApply apply = adoptApplyMapper.selectById(auditDTO.getId());
        if (apply == null) {
            throw new BusinessException("领养申请不存在");
        }
        if (apply.getStatus() != null && apply.getStatus() != 0) {
            throw new BusinessException("该申请已审核，不能重复审核");
        }

        if (auditDTO.getStatus() == 1) {
            // 审核通过
            apply.setStatus(1);
            adoptApplyMapper.updateById(apply);

            // 自动生成领养记录：领养人取申请人，领养日期取当天
            AdoptRecord record = new AdoptRecord();
            // 领养记录为审核通过时新建，主键同样由数据库自增生成
            record.setId(null);
            record.setPetId(apply.getPetId());
            record.setPetName(apply.getPetName());
            record.setAdopterName(apply.getApplicantName());
            record.setAdopterPhone(apply.getPhone());
            record.setAdoptDate(LocalDate.now());
            record.setCreateTime(LocalDateTime.now());
            adoptRecordMapper.insert(record);

            // 对应宠物状态置为 2 已领养
            Pet pet = petMapper.selectById(apply.getPetId());
            if (pet == null) {
                // 宠物已被删除时无法完成领养流转，直接抛异常回滚本次审核
                throw new BusinessException("申请领养的宠物不存在，无法通过审核");
            }
            pet.setStatus(2);
            petMapper.updateById(pet);
        } else if (auditDTO.getStatus() == 2) {
            // 审核驳回，保存驳回原因
            apply.setStatus(2);
            apply.setRejectReason(auditDTO.getRejectReason());
            adoptApplyMapper.updateById(apply);
        } else {
            throw new BusinessException("审核状态只能是1通过/2驳回");
        }
    }

    /**
     * 生成申请编号：AP + yyyyMMddHHmmss + 4位随机数
     */
    private String generateApplyNo() {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "AP" + LocalDateTime.now().format(APPLY_NO_FORMAT) + random;
    }

}
