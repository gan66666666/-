package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 领养记录分页查询入参
 */
@Data
public class RecordQueryDTO implements Serializable {

    /** 页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 10;

    /** 领养人姓名，模糊匹配 */
    private String adopterName;

    /** 领养日期开始（含当天），兼容 "yyyy-MM-dd"、"yyyy-MM-dd HH:mm:ss"、"yyyy-MM-dd'T'HH:mm:ss" */
    private LocalDate startDate;

    /** 领养日期结束（含当天），兼容 "yyyy-MM-dd"、"yyyy-MM-dd HH:mm:ss"、"yyyy-MM-dd'T'HH:mm:ss" */
    private LocalDate endDate;
}
