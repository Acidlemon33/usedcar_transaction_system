package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.CarInfoPO;
import com.usedcar.repository.CarInfoRepository;
import com.usedcar.service.CarInfoService;
import org.springframework.stereotype.Service;

@Service
public class CarInfoServiceImpl extends ServiceImpl<CarInfoRepository, CarInfoPO> implements CarInfoService {
    @Override
    public IPage<CarInfoPO> paginQuery(CarInfoPO carInfo, Long pageNum, Long pageSize) {
        Page<CarInfoPO> page = new Page<>(pageNum, pageSize);
        return this.page(page, null);
    }
}