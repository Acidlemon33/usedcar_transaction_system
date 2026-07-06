DELIMITER //
CREATE PROCEDURE sp_process_payment(
    IN p_order_id BIGINT,
    IN p_buyer_id BIGINT,
    OUT p_msg VARCHAR(128)
)
BEGIN
    DECLARE v_price DOUBLE;
    DECLARE v_balance DOUBLE;
    
    -- 1. 获取订单价格
    SELECT transaction_price INTO v_price FROM transaction_order WHERE order_id = p_order_id;
    -- 2. 获取买家余额
    SELECT balance INTO v_balance FROM user WHERE user_id = p_buyer_id;
    
    IF v_balance >= v_price THEN
        -- 扣款
        UPDATE user SET balance = balance - v_price WHERE user_id = p_buyer_id;
        -- 更新订单状态为1(已支付)
        UPDATE transaction_order SET transaction_status = 1, payment_time = NOW() WHERE order_id = p_order_id;
        SET p_msg = '支付成功';
    ELSE
        SET p_msg = '余额不足';
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_withdraw_car(IN p_sale_id BIGINT)
BEGIN
    -- 将状态改为下架
    UPDATE sale_info SET car_status = '下架', update_time = NOW() WHERE sale_info_id = p_sale_id;
END //
DELIMITER ;