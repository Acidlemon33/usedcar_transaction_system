package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 二手车信息;数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("car_info")
public class CarInfoPO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 车辆 ID，车辆表记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long carId;

    /** 车辆品牌，车辆品牌类别（外键） */
    private String brand;

    /** 车型，车辆的具体型号 */
    private String model;

    /** 上牌年份，首次上牌日期 */
    private LocalDate carYear;

    /** 行驶里程，行驶公里数（单位：公里） */
    private Double mileage;

    /** 售价，卖家的预期售价 */
    private Double sellPrice;

    /** 事故记录，是否发生过事故 */
    private Integer accident;

    /** 创建时间，车辆信息发布时间 */
    private LocalDateTime createTime;

    /** 更新时间，车辆信息更新时间 */
    private LocalDateTime updateTime;

    /** 内饰颜色 */
    private String intColor;

    /** 外观颜色 */
    private String extColor;

    /** 干净产权，1有干净产权，0无干净产权 */
    private Integer cleanTitle;

    /** 燃油类型 */
    private String fuelType;

    /** 引擎马力 */
    private String engineHp;

    /** 引擎气缸数 */
    private String engineLiters;

    /** 卖家用户ID，关联 user(user_id) */
    private Long sellerId;
}
