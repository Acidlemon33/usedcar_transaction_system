package com.usedcar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.CommentPO;
import com.usedcar.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping
    public Result<Boolean> save(@RequestBody CommentPO comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        return Result.success(commentService.save(comment));
    }

    /**
     * 分页查询评论列表 (可按订单ID或用户ID查询)
     */
    @GetMapping("/page")
    public Result<IPage<CommentPO>> page(
            CommentPO comment, // 前端可以传 order_id 或 user_id
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return Result.success(commentService.paginQuery(comment, pageNum, pageSize));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{commentId}")
    public Result<Boolean> remove(@PathVariable Long commentId) {
        return Result.success(commentService.removeById(commentId));
    }
}