-- WeQueue Food Queue Management - MySQL schema + sample data
-- Import: mysql -u root -p < database.sql

CREATE DATABASE IF NOT EXISTS wequeue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wequeue;

DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS queues;
DROP TABLE IF EXISTS queue_counter;
DROP TABLE IF EXISTS foods;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('customer', 'staff', 'admin') NOT NULL DEFAULT 'customer',
    profile_picture VARCHAR(500) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE foods (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    image_url VARCHAR(500) DEFAULT NULL,
    category VARCHAR(80) DEFAULT 'General',
    is_available TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE queue_counter (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE queues (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    queue_number VARCHAR(20) NOT NULL,
    status ENUM('waiting', 'serving', 'completed', 'cancelled') NOT NULL DEFAULT 'waiting',
    food_id INT UNSIGNED DEFAULT NULL,
    served_at TIMESTAMP NULL DEFAULT NULL,
    completed_at TIMESTAMP NULL DEFAULT NULL,
    cancelled_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE SET NULL,
    UNIQUE KEY uk_queue_number (queue_number),
    INDEX idx_queues_status (status),
    INDEX idx_queues_user (user_id),
    INDEX idx_queues_created (created_at),
    INDEX idx_queues_updated (updated_at)
) ENGINE=InnoDB;

CREATE TABLE favorites (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    food_id INT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_food (user_id, food_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE tokens (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id INT UNSIGNED NOT NULL,
    token VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_tokens_token (token)
) ENGINE=InnoDB;

-- Sample users
-- Passwords:
-- customer@wequeue.com = customer123
-- staff@foodqueue.com = staff123
-- admin@foodqueue.com = admin123
-- admin@wequeue.com = admin123
INSERT INTO users (name, email, password, role, profile_picture) VALUES
('Customer Demo', 'customer@wequeue.com', '$2y$12$iw9pop0OIg4uXAQT79TFGuQf.hT/w/gE8JOZAmEYq5bDR.xty5NOS', 'customer', NULL),
('Staff Operator', 'staff@foodqueue.com', '$2y$12$Qs1UDHcKOEXEvjoo9OD1lu3yCYkLSFofXYWxzs8U0fd3Yt8A15Lpm', 'staff', NULL),
('Admin', 'admin@foodqueue.com', '$2y$12$Ll5CKlISa3sOvxTd2cc71O.s/xo3MySZrWrJ6TWA/u.v1JzqiA1vy', 'admin', NULL),
('Admin User', 'admin@wequeue.com', '$2y$12$Ll5CKlISa3sOvxTd2cc71O.s/xo3MySZrWrJ6TWA/u.v1JzqiA1vy', 'admin', NULL);

INSERT INTO foods (name, description, price, image_url, category) VALUES
('Caramel Macchiato', 'Rich espresso with vanilla and caramel drizzle', 45000.00, NULL, 'Beverages'),
('Avocado Toast', 'Sourdough toast with smashed avocado and poached egg', 52000.00, NULL, 'Breakfast'),
('Spicy Ramen Bowl', 'Tonkotsu broth with chili oil and soft-boiled egg', 68000.00, NULL, 'Noodles'),
('Matcha Latte', 'Ceremonial grade matcha with steamed oat milk', 42000.00, NULL, 'Beverages'),
('Chicken Teriyaki', 'Grilled chicken glazed with teriyaki sauce and rice', 55000.00, NULL, 'Main Course');

-- Queue counter seed keeps the next generated queue number after the sample queues.
INSERT INTO queue_counter (id) VALUES (1), (2), (3);
ALTER TABLE queue_counter AUTO_INCREMENT = 4;

-- Sample queues. Each user has at most one active queue.
INSERT INTO queues (user_id, queue_number, status, food_id, served_at, completed_at, cancelled_at, created_at, updated_at) VALUES
(1, 'A001', 'completed', 1, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 3 MINUTE, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 MINUTE, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 MINUTE),
(1, 'A002', 'cancelled', 3, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 4 MINUTE, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 4 MINUTE),
(1, 'A003', 'waiting', 4, NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE));

INSERT INTO favorites (user_id, food_id) VALUES
(1, 1),
(1, 3),
(1, 4);