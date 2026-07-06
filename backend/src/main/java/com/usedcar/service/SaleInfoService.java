package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.SaleInfoPO;

public interface SaleInfoService extends IService<SaleInfoPO> {
    IPage<SaleInfoPO> paginQuery(SaleInfoPO saleInfo, Long pageNum, Long pageSize);
}