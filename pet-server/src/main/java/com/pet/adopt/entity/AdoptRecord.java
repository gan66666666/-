package com.pet.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 领养记录实体，对应 adopt_record 表
 */
@Data
@TableName("adopt_record")
public class AdoptRecord implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 宠物ID */
    @TableField("pet_id")
    private Long petId;

    /** 宠物名字 */
    @TableField("pet_name")
    private String petName;

    /** 领养人姓名 */
    @TableField("adopter_name")
    private String adopterName;

    /** 领养人电话 */
    @TableField("adopter_phone")
    private String adopterPhone;

    /** 领养日期 */
    @TableField("adopt_date")
    private LocalDate adoptDate;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
