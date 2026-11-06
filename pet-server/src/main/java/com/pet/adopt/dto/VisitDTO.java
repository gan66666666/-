package com.pet.adopt.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pet.adopt.util.LocalDateDeserializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 回访计划新增/登记入参
 */
@Data
public class VisitDTO implements Serializable {

    /** 回访ID，登记回访结果时必填 */
    private Long id;

    /** 宠物名字 */
    private String petName;

    /** 领养人姓名 */
    private String adopterName;

    /** 计划回访日期，兼容 "yyyy-MM-dd"、"yyyy-MM-dd HH:mm:ss"、"yyyy-MM-dd'T'HH:mm:ss" */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate planTime;

    /** 回访内容 */
    private String content;

    /** 状态：0待回访 1已完成 */
    private Integer status;
}
