package com.usedcar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.FavoritePO;
import com.usedcar.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    /**
     * 查询单条收藏记录
     */
    @GetMapping("/{favoriteId}")
    public Result<FavoritePO> getById(@PathVariable Long favoriteId) {
        return Result.success(favoriteService.getById(favoriteId));
    }

    /**
     * 分页查询我的收藏
     * 前端传：user_id=1&pageNum=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<IPage<FavoritePO>> page(
            FavoritePO favorite,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return Result.success(favoriteService.paginQuery(favorite, pageNum, pageSize));
    }

    /**
     * 添加收藏
     */
    @PostMapping
    public Result<Boolean> save(@RequestBody FavoritePO favorite) {
        // 自动补全业务逻辑
        favorite.setStatus(1); // 1 表示已收藏
        favorite.setCreateTime(LocalDateTime.now());
        favorite.setUpdateTime(LocalDateTime.now());

        // 建议增加逻辑：如果已经收藏过，则不再重复插入（可选）
        return Result.success(favoriteService.save(favorite));
    }

    /**
     * 修改收藏状态（比如从收藏 1 改为取消收藏 0）
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody FavoritePO favorite) {
        favorite.setUpdateTime(LocalDateTime.now());
        return Result.success(favoriteService.updateById(favorite));
    }

    /**
     * 删除收藏记录
     */
    @DeleteMapping("/{favoriteId}")
    public Result<Boolean> remove(@PathVariable Long favoriteId) {
        return Result.success(favoriteService.removeById(favoriteId));
    }
}