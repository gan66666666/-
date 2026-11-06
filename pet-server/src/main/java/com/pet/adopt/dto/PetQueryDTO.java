package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 宠物分页查询入参
 */
@Data
public class PetQueryDTO implements Serializable {

    /** 页码，默认第 1 页 */
    private Integer pageNum = 1;

    /** 每页条数，默认 10 条 */
    private Integer pageSize = 10;

    /** 宠物类型，精确筛选 */
    private String type;

    /** 领养状态，精确筛选 */
    private Integer status;

    /** 关键字，模糊匹配名称或品种 */
    private String keyword;
}
