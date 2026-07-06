package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易订单表;数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("transaction_order")
public class TransactionOrderPO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 订单ID，订单表唯一标识 */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /** 订单编号，唯一交易订单号 */
    private String orderReference;

    /** 买家ID，购买方用户ID（外键） */
    private String buyerId;

    /** 卖家ID，售卖方用户ID（外键） */
    private String sellerId;

    /** 成交价，买卖双方达成的最终价格 */
    private BigDecimal transactionPrice;

    /** 订单状态，0:待支付 1:已支付 2:已成交 3:售后中 */
    private Integer transactionState;

    /** 付款时间，用户完成支付的时间 */
    private LocalDateTime paymentTime;

    /** 创建时间，订单创建时间 */
    private LocalDateTime createTime;

    /** 更新时间，订单状态更新时间 */
    private LocalDateTime updateTime;

    /** 车辆ID */
    private String carId;

    /** 预测偏差，预测价格与成交价格的偏差率 */
    private BigDecimal predictMatch;

}
