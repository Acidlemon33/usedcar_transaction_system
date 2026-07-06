-- 给 car_info 表中没有 seller_id 的旧数据补充卖家ID
-- 假设这些车辆属于已有的测试用户（user_id=1 的 zhangsan）
UPDATE car_info SET seller_id = 1 WHERE seller_id IS NULL;

-- 验证：查看修复后所有车辆的 seller_id
SELECT car_id, brand, model, seller_id FROM car_info;