package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员;数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("admin")
public class AdminPO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 管理员ID，管理员的唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long adminId;

    /** 用户名，后台管理用户名 */
    private String adminName;

    /** 密码 */
    private String password;

    /** 管理员状态，1表示在职，0表示离职/禁用 */
    private Integer state;

    /** 电子邮箱，联系邮箱 */
    private String mail;

    /** 创建时间，账户创建时间 */
    private LocalDateTime createTime;

    /** 更新时间，账户更新时间 */
    private LocalDateTime updateTime;
}