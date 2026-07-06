package com.usedcar.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usedcar.po.MLPredictPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MLPredictRepository extends BaseMapper<MLPredictPO> {
}