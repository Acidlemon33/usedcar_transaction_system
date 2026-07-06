-- 给所有测试用户设置余额，方便测试购买功能
UPDATE `user` SET balance = 10000000 WHERE user_id >= 1;

-- 验证
SELECT user_id, username, balance FROM `user`;