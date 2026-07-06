package com.usedcar.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usedcar.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository extends BaseMapper<UserPO> {
}