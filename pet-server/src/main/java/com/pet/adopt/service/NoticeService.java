package com.pet.adopt.service;

import com.pet.adopt.dto.NoticeDTO;
import com.pet.adopt.dto.NoticeQueryDTO;
import com.pet.adopt.entity.Notice;
import com.pet.adopt.util.PageResult;

/**
 * 公告服务
 */
public interface NoticeService {

    /**
     * 分页查询公告
     */
    PageResult<Notice> pageNotices(NoticeQueryDTO query);

    /**
     * 查询公告详情
     */
    Notice getNoticeById(Long id);

    /**
     * 新增公告
     */
    void addNotice(NoticeDTO noticeDTO);

    /**
     * 修改公告
     */
    void updateNotice(NoticeDTO noticeDTO);

    /**
     * 删除公告
     */
    void deleteNotice(Long id);

    /**
     * 切换公告置顶状态
     */
    void toggleTop(Long id);
}
