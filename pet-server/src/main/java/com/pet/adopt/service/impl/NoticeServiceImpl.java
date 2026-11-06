package com.pet.adopt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.adopt.dto.NoticeDTO;
import com.pet.adopt.dto.NoticeQueryDTO;
import com.pet.adopt.entity.Notice;
import com.pet.adopt.exception.BusinessException;
import com.pet.adopt.mapper.NoticeMapper;
import com.pet.adopt.service.NoticeService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeMapper noticeMapper;

    @Override
    public PageResult<Notice> pageNotices(NoticeQueryDTO query) {
        NoticeQueryDTO safeQuery = query == null ? new NoticeQueryDTO() : query;
        long pageNum = safeQuery.getPageNum() == null || safeQuery.getPageNum() < 1
                ? 1L : safeQuery.getPageNum();
        long pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1
                ? 10L : safeQuery.getPageSize();

        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(safeQuery.getKeyword())) {
            wrapper.like(Notice::getTitle, safeQuery.getKeyword().trim());
        }
        // 排序规则：置顶公告排最前，其余按创建时间倒序
        wrapper.last("ORDER BY is_top DESC, create_time DESC, id DESC");

        Page<Notice> page = noticeMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<Notice> records = page.getRecords();
        return PageResult.of(page.getTotal(), records);
    }

    @Override
    public Notice getNoticeById(Long id) {
        if (id == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        return notice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addNotice(NoticeDTO noticeDTO) {
        if (noticeDTO == null
                || !StringUtils.hasText(noticeDTO.getTitle())
                || !StringUtils.hasText(noticeDTO.getContent())) {
            throw new BusinessException("公告标题和内容不能为空");
        }
        // title 是 VARCHAR(100)、content 是 TEXT，超长直接给友好提示
        ValidationUtil.maxLength(noticeDTO.getTitle(), 100, "公告标题");
        ValidationUtil.maxLength(noticeDTO.getContent(), 16000, "公告内容");

        Notice notice = new Notice();
        // 新增场景忽略前端传入的 id，主键一律交由数据库自增生成，避免主键冲突与数据错乱
        notice.setId(null);
        notice.setTitle(noticeDTO.getTitle().trim());
        notice.setContent(noticeDTO.getContent());
        notice.setIsTop(noticeDTO.getIsTop() == null ? 0 : noticeDTO.getIsTop());
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotice(NoticeDTO noticeDTO) {
        if (noticeDTO == null || noticeDTO.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        if (!StringUtils.hasText(noticeDTO.getTitle()) || !StringUtils.hasText(noticeDTO.getContent())) {
            throw new BusinessException("公告标题和内容不能为空");
        }
        ValidationUtil.maxLength(noticeDTO.getTitle(), 100, "公告标题");
        ValidationUtil.maxLength(noticeDTO.getContent(), 16000, "公告内容");
        if (noticeMapper.selectById(noticeDTO.getId()) == null) {
            throw new BusinessException("公告不存在");
        }

        Notice notice = new Notice();
        notice.setId(noticeDTO.getId());
        notice.setTitle(noticeDTO.getTitle().trim());
        notice.setContent(noticeDTO.getContent());
        if (noticeDTO.getIsTop() != null) {
            notice.setIsTop(noticeDTO.getIsTop());
        }
        noticeMapper.updateById(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotice(Long id) {
        if (id == null) {
            throw new BusinessException("公告ID不能为空");
        }
        if (noticeMapper.selectById(id) == null) {
            throw new BusinessException("公告不存在");
        }
        noticeMapper.deleteById(id);
    }

    /**
     * 切换置顶状态：0 置为 1，1 置为 0
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long id) {
        Notice exist = getNoticeById(id);

        Notice notice = new Notice();
        notice.setId(id);
        notice.setIsTop(exist.getIsTop() != null && exist.getIsTop() == 1 ? 0 : 1);
        noticeMapper.updateById(notice);
    }
}
