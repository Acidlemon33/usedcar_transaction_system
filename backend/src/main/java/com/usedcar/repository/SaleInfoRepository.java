package com.usedcar.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usedcar.po.SaleInfoPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SaleInfoRepository extends BaseMapper<SaleInfoPO> {
}