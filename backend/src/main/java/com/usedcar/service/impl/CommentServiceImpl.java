package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.CommentPO;
import com.usedcar.repository.CommentRepository;
import com.usedcar.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentRepository, CommentPO> implements CommentService {

    @Override
    public IPage<CommentPO> paginQuery(CommentPO comment, Long pageNum, Long pageSize) {
        Page<CommentPO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<CommentPO> wrapper = new LambdaQueryWrapper<>();

        // 如果前端传了订单ID，就只查这个订单的评论
        wrapper.eq(comment.getOrderId() != null, CommentPO::getOrderId, comment.getOrderId());

        // 如果传了用户ID，就只查这个用户发出的评论
        wrapper.eq(comment.getUserId() != null, CommentPO::getUserId, comment.getUserId());

        // 按照创建时间倒序
        wrapper.orderByDesc(CommentPO::getCreateTime);

        return this.page(page, wrapper);
    }
}