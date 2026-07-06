package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.SaleInfoPO;
import com.usedcar.repository.SaleInfoRepository;
import com.usedcar.service.SaleInfoService;
import org.springframework.stereotype.Service;

@Service
public class SaleInfoServiceImpl extends ServiceImpl<SaleInfoRepository, SaleInfoPO> implements SaleInfoService {
    @Override
    public IPage<SaleInfoPO> paginQuery(SaleInfoPO saleInfo, Long pageNum, Long pageSize) {
        Page<SaleInfoPO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }
}