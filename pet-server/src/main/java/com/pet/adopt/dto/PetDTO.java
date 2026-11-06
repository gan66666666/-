package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 宠物新增/修改入参
 */
@Data
public class PetDTO implements Serializable {

    /** 宠物ID，修改时必填 */
    private Long id;

    /** 宠物名称 */
    private String name;

    /** 宠物类型：狗狗/猫咪/兔子/其他 */
    private String type;

    /** 品种 */
    private String breed;

    /** 年龄（月），与管理端表单“单位：月”及数据库字段注释保持一致 */
    private Integer age;

    /** 性别 */
    private String gender;

    /** 头像地址 */
    private String avatar;

    /** 健康状况 */
    private String health;

    /** 领养状态：1可领养/2已领养/3救助中 */
    private Integer status;
}
