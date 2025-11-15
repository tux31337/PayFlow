-- ============================================
-- Flyway 마이그레이션 V1: 테이블 생성
-- ============================================

-- Users 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    sign_up_type VARCHAR(20) NOT NULL,
    password VARCHAR(100),
    social_id VARCHAR(100),
    social_provider VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Stocks 테이블
CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(20) NOT NULL,
    stock_name VARCHAR(50) NOT NULL,
    market VARCHAR(10) NOT NULL,
    sector VARCHAR(30) NOT NULL,
    current_price DECIMAL(19, 2) NOT NULL,
    price_updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_stock_code UNIQUE (stock_code)
);

-- Stock Price History 테이블
CREATE TABLE IF NOT EXISTS stock_price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(10) NOT NULL,
    trade_time TIMESTAMP NOT NULL,
    current_price BIGINT NOT NULL,
    price_change BIGINT,
    change_rate DOUBLE,
    volume BIGINT,
    created_at TIMESTAMP NOT NULL
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_stock_time ON stock_price_history(stock_code, trade_time);
CREATE INDEX IF NOT EXISTS idx_trade_time ON stock_price_history(trade_time);

-- Transactions 테이블
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stock_code VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Transactions 인덱스
CREATE INDEX IF NOT EXISTS idx_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_stock ON transactions(user_id, stock_code);


