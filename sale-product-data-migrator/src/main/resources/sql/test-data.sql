-- 模拟PHP旧表测试数据
INSERT INTO product_legacy (id, product_name, product_code, status, category_id, brand_id, main_image, price, original_price, is_hot, is_new, target_audience, network_type, origin, warranty_months, extra_info)
VALUES
(1, 'iPhone 16', 'IPHONE16', 1, 101, 201, 'https://img.example.com/iphone16.jpg', 599900, 649900, 1, 0, '年轻白领', '5G', '中国', 12, '{"chip":"A18","waterproof":"IP68"}'),
(2, 'iPhone 16 Pro', 'IPHONE16PRO', 1, 101, 201, 'https://img.example.com/iphone16pro.jpg', 799900, 849900, 1, 1, '商务人士', '5G', '中国', 12, '{"chip":"A18 Pro","waterproof":"IP68"}'),
(3, 'Redmi Note 14', 'REDMI_NOTE14', 1, 102, 202, 'https://img.example.com/redmi14.jpg', 129900, 149900, 0, 1, '学生', '5G', '中国', 12, '{"chip":"天玑7300","waterproof":"IP54"}'),
(4, 'Samsung Galaxy S25', 'SAMSUNG_S25', 1, 103, 203, 'https://img.example.com/s25.jpg', 699900, 749900, 1, 0, '科技爱好者', '5G', '韩国', 24, '{"chip":"骁龙8 Gen4","waterproof":"IP68"}'),
(5, 'OPPO Find X8', 'OPPO_FINDX8', 1, 104, 204, 'https://img.example.com/findx8.jpg', 459900, 499900, 0, 0, '摄影爱好者', '5G', '中国', 12, NULL);

INSERT INTO product_sku_legacy (id, product_id, sku_code, sku_name, price, original_price, status, color, storage, weight, sort_order)
VALUES
(1, 1, 'IPHONE16-BLK-128', 'iPhone 16 黑色 128GB', 599900, 649900, 1, '黑色', '128GB', 170, 1),
(2, 1, 'IPHONE16-BLK-256', 'iPhone 16 黑色 256GB', 699900, 749900, 1, '黑色', '256GB', 170, 2),
(3, 1, 'IPHONE16-WHT-128', 'iPhone 16 白色 128GB', 599900, 649900, 1, '白色', '128GB', 170, 3),
(4, 2, 'IPHONE16PRO-BLK-256', 'iPhone 16 Pro 黑色 256GB', 799900, 849900, 1, '黑色', '256GB', 199, 1),
(5, 2, 'IPHONE16PRO-BLK-512', 'iPhone 16 Pro 黑色 512GB', 999900, 1049900, 1, '黑色', '512GB', 199, 2),
(6, 2, 'IPHONE16PRO-GLD-256', 'iPhone 16 Pro 金色 256GB', 799900, 849900, 1, '金色', '256GB', 199, 3),
(7, 3, 'REDMI14-BLU-128', 'Redmi Note 14 蓝色 128GB', 129900, 149900, 1, '蓝色', '128GB', 190, 1),
(8, 3, 'REDMI14-BLK-256', 'Redmi Note 14 黑色 256GB', 149900, 169900, 1, '黑色', '256GB', 190, 2),
(9, 4, 'S25-BLK-256', 'Galaxy S25 黑色 256GB', 699900, 749900, 1, '黑色', '256GB', 168, 1),
(10, 4, 'S25-WHT-512', 'Galaxy S25 白色 512GB', 799900, 849900, 1, '白色', '512GB', 168, 2),
(11, 5, 'FINDX8-GRN-256', 'OPPO Find X8 绿色 256GB', 459900, 499900, 1, '绿色', '256GB', 193, 1),
(12, 5, 'FINDX8-BLK-512', 'OPPO Find X8 黑色 512GB', 529900, 569900, 1, '黑色', '512GB', 193, 2);
