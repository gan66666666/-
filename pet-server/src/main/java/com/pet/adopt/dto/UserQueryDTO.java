package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户端分页查询入参
 */
@Data
public class UserQueryDTO implements Serializable {

    /** 页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 10;
}

