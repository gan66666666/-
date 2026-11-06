package com.pet.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 宠物实体，对应 pet 表
 */
@Data
@TableName("pet")
public class Pet implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 宠物名称 */
    @TableField("name")
    private String name;

    /** 宠物类型：狗狗/猫咪/兔子/其他 */
    @TableField("type")
    private String type;

    /** 品种 */
    @TableField("breed")
    private String breed;

    /** 年龄（月），与管理端表单“单位：月”及数据库字段注释保持一致 */
    @TableField("age")
    private Integer age;

    /** 性别 */
    @TableField("gender")
    private String gender;

    /** 头像地址 */
    @TableField("avatar")
    private String avatar;

    /** 健康状况 */
    @TableField("health")
    private String health;

    /** 领养状态：1可领养/2已领养/3救助中 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
