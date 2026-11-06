package com.pet.adopt.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 公告新增/修改入参
 */
@Data
public class NoticeDTO implements Serializable {

    /** 公告ID，修改时必填 */
    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 是否置顶：0否 1是 */
    private Integer isTop;
}
