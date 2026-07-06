USE usedcar_transaction_system;

-- 1. 录入用户
INSERT INTO `user` (user_id, username, password, real_name, phone, credit_score, state, balance) VALUES 
(1, 'zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13811112222', 100, 1, 500000.0),
(2, 'lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13933334444', 95, 1, 200000.0),
(3, 'wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13755556666', 100, 1, 1000000.0);

-- 2. 录入车辆信息
INSERT INTO car_info (brand, model, car_year, mileage, sell_price, accident, int_color, ext_color) VALUES 
('BMW', 'X5', '2021-05-20', 3201000, 450000.0, 0, 'Black', 'White'),
('Toyota', 'Camry', '2019-10-12', 58000, 150000.0, 0, 'Beige', 'Black'),
('Tesla', 'Model 3', '2022-01-15', 120000, 210000.0, 1, 'White', 'Blue');

-- 3. 录入管理员
INSERT INTO admin (admin_name, password, state, mail) VALUES 
('admin_master', 'e10adc3949ba59abbe56e057f20f883e', 1, 'master@usedcar.com');

-- 4. 录入出售信息
INSERT INTO sale_info (saler_id, car_id, expect_price, predict_price, car_status, context) VALUES 
(1, 1, 450000.0, 435000.0, '在售', '个人一手宝马，按时保养'),
(2, 2, 150000.0, 142000.0, '在售', '代步神车，省油耐造');

-- 5. 录入预测记录
INSERT INTO ml_predict (car_id, predict_price, accuracy, model_version, user_id) VALUES 
(1, 435000.0, 0.96, 'V1.0', 1),
(2, 142000.0, 0.94, 'V1.0', 2);