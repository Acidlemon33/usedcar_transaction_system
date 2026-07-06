package com.usedcar.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.usedcar.po.FavoritePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteRepository extends BaseMapper<FavoritePO> {
}