package com.pet.adopt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体，对应 notice 表
 */
@Data
@TableName("notice")
public class Notice implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 公告标题 */
    @TableField("title")
    private String title;

    /** 公告内容 */
    @TableField("content")
    private String content;

    /** 是否置顶：0否 1是 */
    @TableField("is_top")
    private Integer isTop;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}
