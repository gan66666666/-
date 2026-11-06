package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 新增领养申请入参
 */
@Data
public class ApplyDTO implements Serializable {

    /** 申请人姓名 */
    private String applicantName;

    /** 联系电话 */
    private String phone;

    /** 申请领养的宠物ID */
    private Long petId;

    /** 宠物名字 */
    private String petName;

    /** 领养理由 */
    private String reason;
}
