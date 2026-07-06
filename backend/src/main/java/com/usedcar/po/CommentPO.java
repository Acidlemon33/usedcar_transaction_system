package com.usedcar.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("comment")
public class CommentPO {
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;
    private Long orderId;
    private Long userId;
    private String score; // 1~5 (对应SQL的ENUM)
    private String content;
    private String reply;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}