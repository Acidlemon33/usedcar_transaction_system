CREATE VIEW view_market_cars AS
SELECT 
    s.sale_info_id,
    c.car_id,
    c.brand,
    c.model,
    c.car_year,
    c.mileage,
    s.expect_price,
    s.predict_price,
    s.context AS seller_description,
    u.username AS seller_name
FROM sale_info s
JOIN car_info c ON s.car_id = c.car_id
JOIN user u ON s.saler_id = u.user_id
WHERE s.car_status = '在售';

CREATE VIEW view_user_order_details AS
SELECT 
    o.order_reference,
    b.username AS buyer_name,
    s.username AS seller_name,
    c.brand,
    c.model,
    o.transaction_price,
    o.transaction_state,
    o.create_time AS order_date
FROM transaction_order o
JOIN user b ON o.buyer_id = b.user_id
JOIN user s ON o.seller_id = s.user_id
JOIN car_info c ON o.car_id = c.car_id;