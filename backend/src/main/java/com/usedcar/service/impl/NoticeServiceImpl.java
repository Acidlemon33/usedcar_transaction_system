package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.NoticePO;
import com.usedcar.repository.NoticeRepository;
import com.usedcar.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeRepository, NoticePO> implements NoticeService {
    @Override
    public IPage<NoticePO> paginQuery(NoticePO notice, Long pageNum, Long pageSize) {
        Page<NoticePO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }
}