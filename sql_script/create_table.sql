CREATE DATABASE IF NOT EXISTS usedcar_transaction_system DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE usedcar_transaction_system;

-- 1. 用户表 user
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`(
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID;用户唯一 ID',
    `username` VARCHAR(128) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码;加密密码',
    `real_name` VARCHAR(128) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(128) NOT NULL COMMENT '手机号',
    `credit_score` INT NOT NULL COMMENT '信用分',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间;注册时间',
    `state` TINYINT NOT NULL COMMENT '状态;1表示正常，0表示封停',
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户;用户表';

ALTER TABLE `user`
ADD COLUMN balance DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '用户账户余额';

ALTER TABLE `user` ADD UNIQUE (username);

-- 2. 车辆信息表 car_info

DROP TABLE IF EXISTS car_info;
CREATE TABLE car_info(
    `car_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '车辆 ID;车辆表记录唯一标识',
    `brand` VARCHAR(128) NOT NULL COMMENT '车辆品牌;车辆品牌类别',
    `model` VARCHAR(128) NOT NULL COMMENT '车型;车辆的具体型号',
    `car_year` DATE NOT NULL COMMENT '上牌年份;首次上牌日期',
    `mileage` DOUBLE NOT NULL COMMENT '行驶里程;行驶公里数（单位：公里）',
    `sell_price` DOUBLE NOT NULL COMMENT '售价;卖家的预期售价',
    
    `accident` TINYINT COMMENT '事故记录;0无事故，1有事故',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间;车辆信息发布时间',
    `update_time` DATETIME NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间;车辆信息更新时间',
    `int_color` VARCHAR(128) COMMENT '内饰颜色',
    `ext_color` VARCHAR(128) COMMENT '外观颜色',
    `clean_title` TINYINT COMMENT '干净产权;1有干净产权，0无干净产权',
    `fuel_type` VARCHAR(64) COMMENT '燃油类型',
    `engine_hp` VARCHAR(64) COMMENT '引擎马力',
    `engine_liters` VARCHAR(64) COMMENT '引擎排量',
    PRIMARY KEY (`car_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二手车信息';

ALTER TABLE car_info MODIFY create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE car_info MODIFY update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 3. 管理员表 admin
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`(
    `admin_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID;管理员的唯一标识',
    `admin_name` VARCHAR(128) NOT NULL COMMENT '用户名;后台管理用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码;密码',
    `state` TINYINT NOT NULL COMMENT '管理员状态;1表示在职，0表示离职/禁用',
    `mail` VARCHAR(128) NOT NULL COMMENT '电子邮箱;联系邮箱',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间;账户创建时间',
    `update_time` DATETIME NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间;账户更新时间',
    PRIMARY KEY (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员';
ALTER TABLE `admin`
MODIFY COLUMN `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
MODIFY COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 4. 出售信息表 sale_info
DROP TABLE IF EXISTS sale_info;
CREATE TABLE sale_info(
    `sale_info_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '出售信息ID;卖家发布的出售信息ID唯一标识符',
    `saler_id` BIGINT NOT NULL COMMENT '发布信息卖家ID',
    `car_id` BIGINT NOT NULL COMMENT '车辆信息ID',
    `expect_price` DOUBLE COMMENT '期望价格;卖家期望的价格',
    `predict_price` DOUBLE NOT NULL COMMENT '预测价格;模型预测价格',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间;出售信息发布的时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间;出售信息更新的时间',
    `context` TEXT COMMENT '卖家说明;卖家描述',
    `car_status` VARCHAR(32) NOT NULL COMMENT '状态;待审核、在售、已售、下架',
    PRIMARY KEY (`sale_info_id`),
    FOREIGN KEY fk_sale_saler_user (`saler_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY fk_sale_car_carinfo (`car_id`) REFERENCES car_info(`car_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出售信息';

-- 5. 机器学习预测表 ml_predict
DROP TABLE IF EXISTS ml_predict;
CREATE TABLE ml_predict(
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预测记录ID;价格预测记录唯一标识',
    `car_id` BIGINT NOT NULL COMMENT '车辆编号;发起预测的车辆（外键）',
    `predict_price` DOUBLE NOT NULL COMMENT '预测市场价;机器学习模型生成的推荐成交价',
    `accuracy` DOUBLE NOT NULL COMMENT '置信度;算法预测结果的准确度评分',
    `model_version` VARCHAR(128) NOT NULL COMMENT '预测模型版本;使用的预测模型版本号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID;发起预测的用户ID（外键）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间;预测操作生成时间',
    PRIMARY KEY (`record_id`),
    FOREIGN KEY fk_predict_car_carinfo (`car_id`) REFERENCES car_info(`car_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY fk_predict_user_user (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机器学习价格预测表';

ALTER TABLE sale_info
ADD COLUMN record_id BIGINT COMMENT '本次出售对应的价格预测记录ID',
ADD CONSTRAINT fk_sale_mlpredict_mlpredict 
FOREIGN KEY (`record_id`) REFERENCES ml_predict(`record_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 6. 收藏表 favorite
DROP TABLE IF EXISTS favorite;
CREATE TABLE favorite(
    `favorite_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏记录唯一编号',
    `user_id` BIGINT NOT NULL COMMENT '收藏用户ID，关联 user(user_id)',
    `car_id` BIGINT NOT NULL COMMENT '收藏车辆ID，关联 car(car_id)',
    `status` TINYINT NOT NULL COMMENT '收藏状态（1：已收藏，0：已取消）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`favorite_id`),
    FOREIGN KEY fk_fav_user_user (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY fk_fav_car_carinfo (`car_id`) REFERENCES car_info(`car_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏信息';

-- 7. 交易订单表 transaction_order
DROP TABLE IF EXISTS transaction_order;
CREATE TABLE transaction_order(
    `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID;订单表唯一标识',
    `order_reference` VARCHAR(128) NOT NULL COMMENT '订单编号;唯一交易订单号',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID;购买方用户ID（外键）',
    `seller_id` BIGINT COMMENT '卖家ID;售卖方用户ID（外键）',
    `transaction_price` DOUBLE COMMENT '成交价;买卖双方达成的最终价格',
    `transaction_state` TINYINT NOT NULL COMMENT '订单状态;0:待支付 1:已支付 2:已成交 3:售后中',
    `payment_time` DATETIME COMMENT '付款时间;用户完成支付的时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间;订单创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间;订单状态更新时间',
    `car_id` BIGINT COMMENT '车辆ID',
    `predict_match` DOUBLE COMMENT '预测偏差;预测价格与成交价格的偏差率',
    PRIMARY KEY (`order_id`),
    FOREIGN KEY fk_order_buyer_user (`buyer_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY fk_order_seller_user (`seller_id`) REFERENCES `user`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY fk_order_car_carinfo (`car_id`) REFERENCES car_info(`car_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

ALTER TABLE transaction_order
ADD COLUMN sale_info_id BIGINT COMMENT '对应出售信息ID',
ADD CONSTRAINT fk_order_saleinfo_saleinfo FOREIGN KEY (`sale_info_id`) REFERENCES sale_info(`sale_info_id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE transaction_order
ADD COLUMN record_id BIGINT COMMENT '本次成交对应的价格预测记录ID(外键)',
ADD CONSTRAINT fk_order_mlpredict_mlpredict 
FOREIGN KEY (`record_id`) REFERENCES ml_predict(`record_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 8. 评论表 comment
DROP TABLE IF EXISTS comment;
CREATE TABLE comment(
    `comment_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论编号',
    `order_id` BIGINT NOT NULL COMMENT '对应订单编号',
    `user_id` BIGINT NOT NULL COMMENT '评论用户编号',
    `score` ENUM('1','2','3','4','5') COMMENT '评分（1~5）',
    `content` TEXT COMMENT '评论内容',
    `reply` TEXT COMMENT '被评论者回复',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`comment_id`),
    FOREIGN KEY fk_comment_order_order (`order_id`) REFERENCES transaction_order(`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY fk_comment_user_user (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 9. 公告表 notice
DROP TABLE IF EXISTS notice;
CREATE TABLE notice(
    `notice_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告编号',
    `admin_id` BIGINT NOT NULL COMMENT '发布管理员编号',
    `title` VARCHAR(128) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `status` TINYINT NOT NULL COMMENT '公告状态（1：发布，0：下架）',
    `publish_time` DATETIME COMMENT '发布时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`notice_id`),
    FOREIGN KEY fk_notice_admin_admin (`admin_id`) REFERENCES `admin`(`admin_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';