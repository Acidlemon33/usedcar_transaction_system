DELIMITER //
CREATE TRIGGER tr_after_order_complete
AFTER UPDATE ON transaction_order
FOR EACH ROW
BEGIN
    IF NEW.transaction_state = 2 THEN
        UPDATE sale_info SET car_status = '已售' WHERE car_id = NEW.car_id;
    END IF;
END //
DELIMITER ;

CREATE TRIGGER tr_before_user_insert
BEFORE INSERT ON user
FOR EACH ROW
BEGIN
    IF NEW.credit_score IS NULL THEN
        SET NEW.credit_score = 100;
    END IF;
    SET NEW.create_time = NOW();
END //
DELIMITER ;