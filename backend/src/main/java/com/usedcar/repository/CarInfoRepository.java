package com.usedcar.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usedcar.po.CarInfoPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CarInfoRepository extends BaseMapper<CarInfoPO> {
}