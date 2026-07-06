package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户;用户表数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("user")
public class UserPO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID，用户唯一 ID */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 用户名 */
    private String username;

    /** 密码，加密密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 信用分 */
    private Integer creditScore;

    /** 创建时间，注册时间 */
    private LocalDateTime createTime;

    /** 状态，1表示正常，0表示封停 */
    private Integer state;
}