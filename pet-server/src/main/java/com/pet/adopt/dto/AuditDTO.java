package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 领养申请审核入参
 */
@Data
public class AuditDTO implements Serializable {

    /** 申请ID */
    private Long id;

    /** 审核结果：1通过 2驳回 */
    private Integer status;

    /** 驳回原因，status=2 时填写 */
    private String rejectReason;
}
