package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.TransactionOrderPO;
import com.usedcar.repository.TransactionOrderRepository;
import com.usedcar.service.TransactionOrderService;
import org.springframework.stereotype.Service;

@Service
public class TransactionOrderServiceImpl extends ServiceImpl<TransactionOrderRepository, TransactionOrderPO> implements TransactionOrderService {
    @Override
    public IPage<TransactionOrderPO> paginQuery(TransactionOrderPO order, Long pageNum, Long pageSize) {
        Page<TransactionOrderPO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }
}