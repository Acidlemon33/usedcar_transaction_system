package com.usedcar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.SaleInfoPO;
import com.usedcar.service.SaleInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出售信息控制器
 */
@RestController
@RequestMapping("/saleInfo")
public class SaleInfoController {
    @Autowired
    private SaleInfoService saleInfoService;

    /**
     * 通过ID查询单条数据
     */
    @GetMapping("/{saleInfoId}")
    public Result<SaleInfoPO> getById(@PathVariable Long saleInfoId) {
        return Result.success(saleInfoService.getById(saleInfoId));
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result<IPage<SaleInfoPO>> paginQuery(
            SaleInfoPO saleInfo,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(saleInfoService.paginQuery(saleInfo, pageNum, pageSize));
    }

    /**
     * 新增数据 (标准保存)
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody SaleInfoPO saleInfo) {
        return Result.success(saleInfoService.save(saleInfo));
    }

    /**
     * 更新数据
     */
    @PutMapping
    public Result<Boolean> edit(@RequestBody SaleInfoPO saleInfo) {
        return Result.success(saleInfoService.updateById(saleInfo));
    }

    /**
     * 通过主键删除数据
     */
    @DeleteMapping("/{saleInfoId}")
    public Result<Boolean> deleteById(@PathVariable Long saleInfoId) {
        return Result.success(saleInfoService.removeById(saleInfoId));
    }

    /**
     * 发布出售信息 (业务发布流程)
     * 关联卖家和车辆，并自动补全状态和时间
     */
    @PostMapping("/publish") // 补全注解
    public Result<Boolean> publishSale(@RequestBody SaleInfoPO sale) {
        sale.setCreateTime(LocalDateTime.now());
        sale.setUpdateTime(LocalDateTime.now());

        // 既然你把状态挪到了这里，代码里必须给它赋值
        sale.setCarStatus("在售");

        // 简单的价格预测模拟
        if (sale.getExpectPrice() != null) {
            sale.setPredictPrice(sale.getExpectPrice());
        }

        return Result.success(saleInfoService.save(sale));
    }

    /**
     * 获取所有出售中的列表 (首页展示)
     */
    @GetMapping("/list")
    public Result<List<SaleInfoPO>> getAllSales() {
        return Result.success(saleInfoService.list());
    }
}