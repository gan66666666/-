package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 领养申请分页查询入参
 */
@Data
public class ApplyQueryDTO implements Serializable {

    /** 页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 10;

    /** 申请状态：0待审核 1通过 2驳回 */
    private Integer status;

    /** 关键字，模糊匹配申请人姓名或申请编号 */
    private String keyword;
}
