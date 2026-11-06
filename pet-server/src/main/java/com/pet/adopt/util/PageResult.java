package com.pet.adopt.util;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回结果封装
 *
 * @param <T> 记录类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private Long total;

    /** 当前页记录集合 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    /**
     * 构造分页结果
 */
    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
