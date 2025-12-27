-- ============================================
-- Flyway 마이그레이션 V1: 전체 테이블 생성
-- ============================================

-- ==================== Users ====================
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

-- ==================== Stocks ====================
CREATE TABLE IF NOT EXISTS stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(20) NOT NULL UNIQUE,
    stock_name VARCHAR(50) NOT NULL,
    market VARCHAR(10) NOT NULL,
    sector VARCHAR(30) NOT NULL,
    current_price DECIMAL(19, 2) NOT NULL,
    price_updated_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- ==================== Portfolios ====================
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    cash_balance DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_portfolios_user_id ON portfolios(user_id);

-- ==================== Holdings ====================
CREATE TABLE IF NOT EXISTS holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    stock_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    average_price DECIMAL(19, 2) NOT NULL,
    total_cost DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_holdings_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE INDEX IF NOT EXISTS idx_holdings_portfolio_id ON holdings(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_holdings_stock_code ON holdings(stock_code);

-- ==================== Orders ====================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    stock_code VARCHAR(20) NOT NULL,
    side VARCHAR(10) NOT NULL,
    type VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    filled_quantity INT NOT NULL DEFAULT 0,
    remaining_quantity INT NOT NULL,
    limit_price DECIMAL(19, 2),
    average_price DECIMAL(19, 2),
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancellation_reason VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_stock_status ON orders(stock_code, status);

-- ==================== Transactions ====================
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

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user_stock ON transactions(user_id, stock_code);
CREATE INDEX IF NOT EXISTS idx_transactions_executed_at ON transactions(executed_at);
