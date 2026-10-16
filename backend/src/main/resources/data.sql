-- Insert Sample Users
INSERT IGNORE INTO users (username, password, full_name, email, role, phone, created_at) VALUES 
('john_doe', '$2a$10$8JJ8v0ZlZqG6QZ6QZ6QZ6Oe5J6J6J6J6J6J6J6J6J6J6J6J6J6', 'John Doe', 'john@example.com', 'USER', '1234567890', CURRENT_TIMESTAMP),
('testuser2', '$2a$10$8JJ8v0ZlZqG6QZ6QZ6QZ6Oe5J6J6J6J6J6J6J6J6J6J6J6J6J6J6', 'Test User Two', 'testuser2@example.com', 'USER', '9998887776', CURRENT_TIMESTAMP);

-- Insert Admin Logins
INSERT IGNORE INTO admins (email, password, name, canteen_name, phone, created_at) VALUES 
('admin1@example.com', '$2a$10$8JJ8v0ZlZqG6QZ6QZ6QZ6Oe5J6J6J6J6J6J6J6J6J6J6J6J6J6J6', 'Admin One', 'Main Canteen', '1111111111', CURRENT_TIMESTAMP),
('admin2@example.com', '$2a$10$8JJ8v0ZlZqG6QZ6QZ6QZ6Oe5J6J6J6J6J6J6J6J6J6J6J6J6J6J6J6', 'Admin Two', 'East Canteen', '2222222222', CURRENT_TIMESTAMP);

-- Dedupe canteens (keep first occurrence by id) and seed defaults
DELETE c1 FROM canteens c1
JOIN canteens c2 ON c1.name = c2.name AND c1.id > c2.id;

INSERT IGNORE INTO canteens (name, location, is_active) VALUES
('Main Canteen', 'Building A, Floor 1', TRUE),
('East Canteen', 'Building B, Floor 2', TRUE);

UPDATE canteens SET is_active = TRUE WHERE name IN ('Main Canteen', 'East Canteen');

-- Insert Menu Items for Main Canteen (canteen_id = 1)
INSERT IGNORE INTO menu_items (name, price, category, emoji, available, canteen_id) VALUES 
('Burger', 5.99, 'Main Course', '🍔', TRUE, 1),
('Pizza', 8.99, 'Main Course', '🍕', TRUE, 1),
('Salad', 4.99, 'Side', '🥗', TRUE, 1),
('Coke', 1.99, 'Beverage', '🥤', TRUE, 1),
('Ice Cream', 2.99, 'Dessert', '🍦', TRUE, 1);

-- Insert Menu Items for East Canteen (canteen_id = 2)
INSERT IGNORE INTO menu_items (name, price, category, emoji, available, canteen_id) VALUES
('Sandwich', 4.99, 'Main Course', '🥪', TRUE, 2),
('Pasta', 7.99, 'Main Course', '🍝', TRUE, 2),
('Fries', 2.99, 'Side', '🍟', TRUE, 2),
('Juice', 2.49, 'Beverage', '🥤', TRUE, 2),
('Cake', 3.99, 'Dessert', '🍰', TRUE, 2);

-- Seed Default Orders so orders survive app restarts (on restart, this INSERT IGNORE is skipped if order already existed)
INSERT IGNORE INTO orders (order_number, user_id, canteen_id, total_amount, status, payment_status, payment_method, restaurant_name, unique_id, transaction_id, order_time)
VALUES ('ORD-SEED-001', 1, 1, 18.97, 'COMPLETED', 'COMPLETED', 'WALLET', 'Main Canteen', 'ORD-SEED-001', 'TXN-SEED-001', NOW());

INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price)
SELECT id, 1, 1, 5.99 FROM orders WHERE order_number = 'ORD-SEED-001'
UNION ALL
SELECT id, 5, 1, 2.99 FROM orders WHERE order_number = 'ORD-SEED-001';

INSERT IGNORE INTO orders (order_number, user_id, canteen_id, total_amount, status, payment_status, payment_method, restaurant_name, unique_id, transaction_id, order_time)
VALUES
('ORD-SEED-002', 1, 2, 16.98, 'COMPLETED', 'COMPLETED', 'ONLINE', 'East Canteen', 'ORD-SEED-002', 'TXN-SEED-002', NOW());

INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price)
SELECT id, 6, 1, 4.99 FROM orders WHERE order_number = 'ORD-SEED-002'
UNION ALL
SELECT id, 8, 1, 7.99 FROM orders WHERE order_number = 'ORD-SEED-002'
UNION ALL
SELECT id, 10, 1, 2.49 FROM orders WHERE order_number = 'ORD-SEED-002';

INSERT IGNORE INTO orders (order_number, user_id, canteen_id, total_amount, status, payment_status, payment_method, restaurant_name, unique_id, transaction_id, order_time)
VALUES
('ORD-SEED-003', 2, 1, 6.98, 'COMPLETED', 'COMPLETED', 'WALLET', 'Main Canteen', 'ORD-SEED-003', 'TXN-SEED-003', NOW());

INSERT IGNORE INTO order_items (order_id, menu_item_id, quantity, unit_price)
SELECT id, 3, 1, 4.99 FROM orders WHERE order_number = 'ORD-SEED-003'
UNION ALL
SELECT id, 4, 1, 1.99 FROM orders WHERE order_number = 'ORD-SEED-003';
