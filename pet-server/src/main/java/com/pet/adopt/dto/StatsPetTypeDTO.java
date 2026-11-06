package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 宠物类型分布数据项
 */
@Data
public class StatsPetTypeDTO implements Serializable {

    /** 宠物类型：狗狗/猫咪/兔子/其他 */
    private String type;

    /** 该类型的宠物数量 */
    private Long count;

    public StatsPetTypeDTO() {
    }

    public StatsPetTypeDTO(String type, Long count) {
        this.type = type;
        this.count = count;
    }
}
