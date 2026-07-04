-- Test schema for H2 in-memory database (MySQL compatibility mode)
-- These tables mirror the production MySQL schema

CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL DEFAULT '',
    avatar VARCHAR(255),
    openid VARCHAR(100),
    register_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS merchant (
    merchant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_name VARCHAR(100) NOT NULL,
    contact_info VARCHAR(50) NOT NULL,
    create_time DATE DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS stall (
    stall_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stall_name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    merchant_id BIGINT
);

CREATE TABLE IF NOT EXISTS dish (
    dish_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dish_name VARCHAR(100) NOT NULL,
    price DECIMAL(8,2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    image_url VARCHAR(255),
    stall_id BIGINT,
    status VARCHAR(20) DEFAULT 'approved'
);

CREATE TABLE IF NOT EXISTS favorite (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    favorite_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS selection_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dish_id BIGINT NOT NULL,
    score INTEGER,
    like_status BOOLEAN,
    select_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
