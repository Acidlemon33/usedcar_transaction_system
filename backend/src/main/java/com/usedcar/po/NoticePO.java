package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告表;数据表的PO对象
 * @date : 2026-7-4
 */
@Data
@TableName("notice")
public class NoticePO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 公告编号 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String noticeId;

    /** 发布管理员编号 */
    private String adminId;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 公告状态（1：发布，0：下架） */
    private Integer status;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}