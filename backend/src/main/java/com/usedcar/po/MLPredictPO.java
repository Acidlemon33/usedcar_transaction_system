package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 机器学习价格预测表;数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("ml_predict")
public class MLPredictPO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 预测记录ID，价格预测记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /** 车辆编号，发起预测的车辆（外键） */
    private Long carId;

    /** 预测市场价，机器学习模型生成的推荐成交价 */
    private Double predictPrice;

    /** 置信度，算法预测结果的准确度评分 */
    private Double accuracy;

    /** 预测模型版本，使用的预测模型版本号 */
    private String modelVersion;

    /** 用户ID，发起预测的用户ID（外键） */
    private Long userId;

    /** 创建时间，预测操作生成时间 */
    private LocalDateTime createTime;
}