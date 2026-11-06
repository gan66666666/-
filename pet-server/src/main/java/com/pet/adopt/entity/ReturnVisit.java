package com.pet.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 回访实体，对应 return_visit 表
 */
@Data
@TableName("return_visit")
public class ReturnVisit implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 宠物名字 */
    @TableField("pet_name")
    private String petName;

    /** 领养人姓名 */
    @TableField("adopter_name")
    private String adopterName;

    /** 计划回访日期（对应 plan_time DATE 列，只精确到天） */
    @TableField("plan_time")
    private LocalDate planTime;

    /** 状态：0待回访 1已完成 */
    @TableField("status")
    private Integer status;

    /** 回访内容 */
    @TableField("content")
    private String content;
}
