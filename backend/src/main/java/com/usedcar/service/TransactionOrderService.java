package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.TransactionOrderPO;

public interface TransactionOrderService extends IService<TransactionOrderPO> {
    IPage<TransactionOrderPO> paginQuery(TransactionOrderPO order, Long pageNum, Long pageSize);
}