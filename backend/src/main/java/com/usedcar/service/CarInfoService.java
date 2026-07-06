package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.CarInfoPO;

public interface CarInfoService extends IService<CarInfoPO> {
    /**
     * 分页查询车辆信息
     * @param carInfo 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    IPage<CarInfoPO> paginQuery(CarInfoPO carInfo, Long pageNum, Long pageSize);
}