package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        LambdaQueryWrapper<CarInfoPO> wrapper = new LambdaQueryWrapper<>();

        // 按卖家筛选
        if (carInfo != null && carInfo.getSellerId() != null) {
            wrapper.eq(CarInfoPO::getSellerId, carInfo.getSellerId());
        }

        // 按品牌搜索
        if (carInfo != null && carInfo.getBrand() != null && !carInfo.getBrand().isEmpty()) {
            wrapper.like(CarInfoPO::getBrand, carInfo.getBrand());
        }

        // 按车型搜索
        if (carInfo != null && carInfo.getModel() != null && !carInfo.getModel().isEmpty()) {
            wrapper.like(CarInfoPO::getModel, carInfo.getModel());
        }

        wrapper.orderByDesc(CarInfoPO::getCreateTime);
        return this.page(page, wrapper);
    }
}
