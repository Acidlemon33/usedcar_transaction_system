package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出售信息;数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("sale_info")
public class SaleInfoPO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 出售信息ID，卖家发布的出售信息ID唯一标识符 */
    @TableId(type = IdType.AUTO)
    private Long saleInfoId;

    private String carStatus;

    /** 发布信息卖家ID */
    private Long salerId;

    /** 车辆信息ID */
    private Long carId;

    /** 期望价格，卖家期望的价格 */
    private BigDecimal expectPrice;

    /** 预测价格，模型预测价格 */
    private BigDecimal predictPrice;

    /** 发布时间，出售信息发布的时间 */
    private LocalDateTime createTime;

    /** 更新时间，出售信息更新的时间 */
    private LocalDateTime updateTime;

    /** 卖家说明，卖家描述 */
    private String context;
}