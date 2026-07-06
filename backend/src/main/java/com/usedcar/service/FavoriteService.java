package com.usedcar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usedcar.po.FavoritePO;

public interface FavoriteService extends IService<FavoritePO> {
    IPage<FavoritePO> paginQuery(FavoritePO favorite, Long pageNum, Long pageSize);
}