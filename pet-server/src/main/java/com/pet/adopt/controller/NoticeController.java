package com.pet.adopt.controller;

import com.pet.adopt.dto.NoticeDTO;
import com.pet.adopt.dto.NoticeQueryDTO;
import com.pet.adopt.entity.Notice;
import com.pet.adopt.service.NoticeService;
import com.pet.adopt.util.PageResult;
import com.pet.adopt.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告接口
 * 注意：server.servlet.context-path=/api，故对外完整路径为 /api/notice/**
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 分页查询公告
     */
    @GetMapping("/page")
    public Result<PageResult<Notice>> page(NoticeQueryDTO query) {
        return Result.success(noticeService.pageNotices(query));
    }

    /**
     * 公告详情
     */
    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable("id") Long id) {
        return Result.success(noticeService.getNoticeById(id));
    }

    /**
     * 新增公告
     */
    @PostMapping
    public Result<Void> add(@RequestBody NoticeDTO noticeDTO) {
        noticeService.addNotice(noticeDTO);
        return Result.success("新增成功", null);
    }

    /**
     * 修改公告
     */
    @PutMapping
    public Result<Void> update(@RequestBody NoticeDTO noticeDTO) {
        noticeService.updateNotice(noticeDTO);
        return Result.success("修改成功", null);
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return Result.success("删除成功", null);
    }

    /**
     * 切换置顶状态
     */
    @PutMapping("/top/{id}")
    public Result<Void> toggleTop(@PathVariable("id") Long id) {
        noticeService.toggleTop(id);
        return Result.success("操作成功", null);
    }
}
