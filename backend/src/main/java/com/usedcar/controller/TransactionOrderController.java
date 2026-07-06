package com.usedcar.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usedcar.common.Result;
import com.usedcar.po.TransactionOrderPO;
import com.usedcar.service.TransactionOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 交易订单控制器
 */
@RestController
@RequestMapping("/transaction_order") // 建议改为下划线命名
public class TransactionOrderController {

    @Autowired
    private TransactionOrderService transactionOrderService;

    /**
     * 根据id查询单条订单
     */
    @GetMapping("/{orderId}")
    public Result<TransactionOrderPO> getById(@PathVariable Long orderId) {
        return Result.success(transactionOrderService.getById(orderId));
    }

    /**
     * 分页查询订单
     */
    @GetMapping("/page")
    public Result<IPage<TransactionOrderPO>> page(
            TransactionOrderPO order,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        IPage<TransactionOrderPO> page = transactionOrderService.paginQuery(order, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 新增订单（下单功能）
     * 自动处理业务逻辑：生成编号、初始状态、时间戳
     */
    @PostMapping
    public Result<Boolean> save(@RequestBody TransactionOrderPO order) {
        // 1. 生成唯一订单编号：ORD + 当前时间毫秒数
        order.setOrderReference("ORD" + System.currentTimeMillis());

        // 2. 设置订单初始状态：0 (待支付)
        order.setTransactionState(0);

        // 3. 补全创建时间和更新时间
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        // 4. 打印日志方便调试
        System.out.println("收到订单：买家ID=" + order.getBuyerId() + ", 车ID=" + order.getCarId());

        return Result.success(transactionOrderService.save(order));
    }

    /**
     * 修改订单状态/信息
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody TransactionOrderPO order) {
        order.setUpdateTime(LocalDateTime.now()); // 每次修改同步更新时间
        return Result.success(transactionOrderService.updateById(order));
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderId}")
    public Result<Boolean> remove(@PathVariable Long orderId) {
        return Result.success(transactionOrderService.removeById(orderId));
    }
}