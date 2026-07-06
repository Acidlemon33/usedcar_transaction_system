package com.usedcar.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
@RequestMapping("/transaction_order")
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
     * 通过订单编号(orderReference)查询
     */
    @GetMapping("/byReference")
    public Result<TransactionOrderPO> getByReference(@RequestParam String ref) {
        LambdaQueryWrapper<TransactionOrderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionOrderPO::getOrderReference, ref);
        TransactionOrderPO order = transactionOrderService.getOne(wrapper, false);
        return Result.success(order);
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
     * 返回完整订单对象（含自增ID）
     */
    @PostMapping
    public Result<TransactionOrderPO> save(@RequestBody TransactionOrderPO order) {
        // 如果前端没有传orderReference，自动生成
        if (order.getOrderReference() == null || order.getOrderReference().isEmpty()) {
            order.setOrderReference("ORD" + System.currentTimeMillis());
        }

        // 设置订单初始状态：0 (待支付)
        order.setTransactionState(0);

        // 补全创建时间和更新时间
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        System.out.println("收到订单：买家ID=" + order.getBuyerId() + ", 车ID=" + order.getCarId() + ", 编号=" + order.getOrderReference());

        // 保存，MyBatis-Plus 会自动填充自增ID到order对象
        transactionOrderService.save(order);
        return Result.success(order);
    }

    /**
     * 修改订单状态/信息
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody TransactionOrderPO order) {
        order.setUpdateTime(LocalDateTime.now());
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