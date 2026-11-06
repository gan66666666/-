package com.pet.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领养申请实体，对应 adopt_apply 表
 */
@Data
@TableName("adopt_apply")
public class AdoptApply implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 提交申请的用户id（普通用户 user_info 主键），历史数据可能为空 */
    @TableField("user_id")
    private Long userId;

    /** 申请编号 */
    @TableField("apply_no")
    private String applyNo;

    /** 申请人姓名 */
    @TableField("applicant_name")
    private String applicantName;

    /** 联系电话 */
    @TableField("phone")
    private String phone;

    /** 申请领养的宠物ID */
    @TableField("pet_id")
    private Long petId;

    /** 宠物名字 */
    @TableField("pet_name")
    private String petName;

    /** 申请状态：0待审核 1通过 2驳回 */
    @TableField("status")
    private Integer status;

    /** 申请时间 */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /** 领养理由 */
    @TableField("reason")
    private String reason;

    /** 驳回原因 */
    @TableField("reject_reason")
    private String rejectReason;
}
