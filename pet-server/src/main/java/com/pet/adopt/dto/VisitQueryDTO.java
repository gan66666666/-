package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 回访分页查询入参
 */
@Data
public class VisitQueryDTO implements Serializable {

    /** 页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 10;

    /** 状态：0待回访 1已完成 */
    private Integer status;
}
