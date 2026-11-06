package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 领养趋势数据点
 */
@Data
public class StatsTrendDTO implements Serializable {

    /** 日期：近7天/近30天为 yyyy-MM-dd，近12个月为 yyyy-MM */
    private String date;

    /** 该日期的成功领养数量，无数据时为 0 */
    private Long count;

    public StatsTrendDTO() {
    }

    public StatsTrendDTO(String date, Long count) {
        this.date = date;
        this.count = count;
    }
}
