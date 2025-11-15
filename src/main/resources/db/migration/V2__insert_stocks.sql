-- ============================================
-- Flyway 마이그레이션 V2: 종목 데이터 삽입
-- 가격은 0원으로 초기화 (앱 시작 시 업데이트)
-- ============================================

-- 종목 데이터 삽입 (가격 0.01원으로 초기화 - CurrentPrice 최소값)
-- H2는 MERGE 문 사용 (중복 시 무시)
-- 앱 시작 시 StockDataInitializer가 실제 가격으로 업데이트
MERGE INTO stocks (stock_code, stock_name, market, sector, current_price, price_updated_at, created_at)
KEY (stock_code)
VALUES
    -- 대형주 - 반도체
    ('005930', '삼성전자', 'KOSPI', '반도체', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('000660', 'SK하이닉스', 'KOSPI', '반도체', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- IT/인터넷
    ('035420', 'NAVER', 'KOSPI', '인터넷', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('035720', '카카오', 'KOSPI', '인터넷', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('036570', '엔씨소프트', 'KOSDAQ', '게임', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- 자동차
    ('005380', '현대차', 'KOSPI', '자동차', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('000270', '기아', 'KOSPI', '자동차', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- 금융
    ('055550', '신한지주', 'KOSPI', '금융', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('105560', 'KB금융', 'KOSPI', '금융', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- 바이오/화학
    ('068270', '셀트리온', 'KOSPI', '바이오', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('207940', '삼성바이오로직스', 'KOSPI', '바이오', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('051910', 'LG화학', 'KOSPI', '화학', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- 유통/식품
    ('028260', '삼성물산', 'KOSPI', '유통', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('097950', 'CJ제일제당', 'KOSPI', '식품', 0.01, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

