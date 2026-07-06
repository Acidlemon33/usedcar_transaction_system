package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏信息;数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("favorite")
public class FavoritePO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 收藏记录唯一编号 */
    @TableId(value = "favorite_id", type = IdType.AUTO)
    private String favoriteId;

    /** 收藏用户ID，关联 user(user_id) */
    private String userId;

    /** 收藏车辆ID，关联 car(car_id) */
    private String carId;

    /** 收藏状态（1：已收藏，0：已取消） */
    private Integer status;

    /** 收藏时间 */
    private LocalDateTime createTime;

    /** 最后更新时间 */
    private LocalDateTime updateTime;
}