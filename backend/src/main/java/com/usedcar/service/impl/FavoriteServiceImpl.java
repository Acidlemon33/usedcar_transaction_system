package com.usedcar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usedcar.po.FavoritePO;
import com.usedcar.repository.FavoriteRepository;
import com.usedcar.service.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteRepository, FavoritePO> implements FavoriteService {

    @Override
    public IPage<FavoritePO> paginQuery(FavoritePO favorite, Long pageNum, Long pageSize) {
        Page<FavoritePO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FavoritePO> wrapper = new LambdaQueryWrapper<>();

        // 如果传了用户ID，只查该用户的收藏
        wrapper.eq(favorite.getUserId() != null, FavoritePO::getUserId, favorite.getUserId());
        // 只查状态为1（已收藏）的记录
        wrapper.eq(FavoritePO::getStatus, 1);
        // 按时间倒序排序
        wrapper.orderByDesc(FavoritePO::getCreateTime);

        return this.page(page, wrapper);
    }
}