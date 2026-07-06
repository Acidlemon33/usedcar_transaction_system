package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.CommentPO;

public interface CommentService extends IService<CommentPO> {
    IPage<CommentPO> paginQuery(CommentPO comment, Long pageNum, Long pageSize);
}