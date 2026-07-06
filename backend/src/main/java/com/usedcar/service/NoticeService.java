package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.NoticePO;

public interface NoticeService extends IService<NoticePO> {
    IPage<NoticePO> paginQuery(NoticePO notice, Long pageNum, Long pageSize);
}