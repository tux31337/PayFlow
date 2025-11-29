-- TimescaleDB 확장 활성화
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- stock_price_history 테이블 생성
-- JPA 엔티티와 호환되도록 id를 PRIMARY KEY로 사용
CREATE TABLE IF NOT EXISTS stock_price_history (
    id BIGSERIAL PRIMARY KEY,
    stock_code VARCHAR(10) NOT NULL,
    trade_time TIMESTAMPTZ NOT NULL,
    current_price BIGINT NOT NULL,
    price_change BIGINT,
    change_rate DOUBLE PRECISION,
    volume BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Hypertable로 변환 (시계열 테이블)
-- trade_time을 기준으로 자동 파티셔닝 (1일 단위 chunk)
SELECT create_hypertable(
    'stock_price_history',
    'trade_time',
    if_not_exists => TRUE,
    chunk_time_interval => INTERVAL '1 day'
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_stock_code ON stock_price_history (stock_code, trade_time DESC);
CREATE INDEX IF NOT EXISTS idx_trade_time ON stock_price_history (trade_time DESC);

-- 압축 정책 설정 (7일 이상 된 데이터 자동 압축)
ALTER TABLE stock_price_history SET (
    timescaledb.compress,
    timescaledb.compress_segmentby = 'stock_code'
);

SELECT add_compression_policy('stock_price_history', INTERVAL '7 days');

-- 데이터 보존 정책 설정 (1년 이상 된 데이터 자동 삭제)
SELECT add_retention_policy('stock_price_history', INTERVAL '1 year');

-- 통계 뷰 생성 (1시간 단위 캔들차트 데이터)
CREATE MATERIALIZED VIEW IF NOT EXISTS stock_price_hourly
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', trade_time) AS bucket,
    stock_code,
    FIRST(current_price, trade_time) AS open_price,
    MAX(current_price) AS high_price,
    MIN(current_price) AS low_price,
    LAST(current_price, trade_time) AS close_price,
    SUM(volume) AS total_volume
FROM stock_price_history
GROUP BY bucket, stock_code
WITH NO DATA;

-- Continuous Aggregate 자동 업데이트 정책
SELECT add_continuous_aggregate_policy('stock_price_hourly',
    start_offset => INTERVAL '3 hours',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour');

-- 권한 설정
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO truvis;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO truvis;