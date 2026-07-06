package com.usedcar.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usedcar.po.AdminPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminRepository extends BaseMapper<AdminPO> {
}