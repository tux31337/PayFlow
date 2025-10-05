# Truvis - AI 투자 거장 분석 플랫폼

> DDD(Domain-Driven Design) 기반 멀티모듈 Spring Boot 프로젝트

## 📦 프로젝트 개요

투자 거장들의 투자 철학과 패턴을 AI로 학습하여, 개인 투자자에게 검증된 투자 관점을 제공하는 교육형 투자 플랫폼

---

## 🏗️ 프로젝트 구조

```
truvis/
├── build.gradle
├── settings.gradle
├── docker-compose.yml
├── src/                           # 메인 애플리케이션 모듈
│   ├── main/
│   │   ├── java/com/truvis/
│   │   │   └── TruvisApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-local.yml
│   └── test/
├── common/                        # ✨ 공통 모듈
├── user/                          # 👤 사용자 모듈
├── master/                        # 🎓 투자 거장 모듈
├── stock/                         # 📈 종목 모듈
├── analysis/                      # 🔍 분석 모듈
├── portfolio/                     # 💼 포트폴리오 모듈
├── question/                      # ❓ 질문 모듈
└── notification/                  # 🔔 알림 모듈
```

---

## 📁 모듈 상세

### ✨ common - 공통 모듈

DDD 기본 클래스 제공, 공통 예외 처리, JWT 기반 인증/인가

<details>
<summary>구조 보기</summary>

```
common/
├── model/                              # 도메인 기본 클래스
│   ├── AggregateRoot.java
│   ├── Entity.java
│   ├── ValueObject.java
│   └── DomainEvent.java
├── exception/                          # 공통 예외
│   ├── BusinessException.java
│   ├── DomainException.java
│   ├── MemberException.java
│   └── EmailVerificationException.java
├── config/                             # 공통 설정
│   ├── CommonConfiguration.java
│   ├── SecurityConfig.java
│   └── GlobalExceptionHandler.java
├── response/                           # 공통 응답 모델
│   ├── ApiResponse.java
│   └── ErrorResponse.java
└── security/                           # 보안 관련
    ├── JwtTokenProvider.java
    ├── JwtAuthenticationFilter.java
    ├── TokenBlacklistService.java
    └── RedisTokenBlacklistService.java
```

</details>

---

### 👤 user - 사용자 모듈

이메일/소셜 로그인, JWT 인증, 이메일 인증, 사용자 프로필 관리

<details>
<summary>구조 보기</summary>

```
user/
├── api/                                # REST API
│   ├── AuthController.java
│   ├── UserController.java
│   ├── SocialAuthController.java
│   └── EmailVerificationController.java
├── model/                              # DTO
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── SignUpRequest.java
│   └── TokenResponse.java
├── application/                        # 서비스
│   ├── AuthService.java
│   ├── UserService.java
│   ├── SocialAuthService.java
│   └── EmailVerificationService.java
├── domain/                             # 도메인
│   ├── User.java
│   ├── Email.java
│   ├── EmailVerification.java
│   └── UserRepository.java
└── infrastructure/                     # 인프라
    ├── JpaUserRepositoryAdapter.java
    ├── RedisRefreshTokenRepository.java
    ├── EmailSender.java
    └── oauth/
        └── KakaoOAuth2Client.java
```

</details>

---

### 🎓 master - 투자 거장 모듈

투자 거장 프로필 관리, 투자 철학 및 기준 제공

<details>
<summary>구조 보기</summary>

```
master/
├── api/
│   └── MasterController.java
├── model/
│   └── MasterResponse.java
├── application/
│   └── MasterApplicationService.java
├── domain/
│   └── InvestmentMaster.java
├── repository/
│   └── InvestmentMasterRepository.java
└── infrastructure/
    └── JpaInvestmentMasterRepository.java
```

</details>

---

### 📈 stock - 종목 모듈

종목 검색 및 조회, 주가 정보 관리, 재무 데이터 저장

<details>
<summary>구조 보기</summary>

```
stock/
├── api/
│   └── StockController.java
├── model/
│   └── StockResponse.java
├── application/
│   └── StockApplicationService.java
├── domain/
│   └── Stock.java
├── repository/
│   └── StockRepository.java
└── infrastructure/
    └── JpaStockRepository.java
```

</details>

---

### 🔍 analysis - 분석 모듈

거장별 종목 분석, 분석 점수 계산, 투자 의견 제공

<details>
<summary>구조 보기</summary>

```
analysis/
├── api/
│   └── AnalysisController.java
├── model/
│   └── AnalysisResponse.java
├── application/
│   └── AnalysisApplicationService.java
├── domain/
│   └── StockAnalysis.java
├── repository/
│   └── StockAnalysisRepository.java
└── infrastructure/
    └── JpaStockAnalysisRepository.java
```

</details>

---

### 💼 portfolio - 포트폴리오 모듈

포트폴리오 생성 및 관리, 보유 종목 추가/제거

<details>
<summary>구조 보기</summary>

```
portfolio/
├── model/
│   └── PortfolioResponse.java
├── application/
│   └── PortfolioApplicationService.java
├── domain/
│   └── Portfolio.java
├── repository/
│   └── PortfolioRepository.java
└── infrastructure/
    └── JpaPortfolioRepository.java
```

</details>

---

### ❓ question - 질문 모듈

RAG 기반 질문 처리, 거장 발언 검색, 추천 질문 생성

<details>
<summary>구조 보기</summary>

```
question/
├── api/
│   └── QuestionController.java
├── model/
│   └── QuestionResponse.java
├── application/
│   └── QuestionApplicationService.java
├── domain/
│   └── UserQuestion.java
├── repository/
│   └── UserQuestionRepository.java
└── infrastructure/
    └── JpaUserQuestionRepository.java
```

</details>

---

### 🔔 notification - 알림 모듈

점수 변동 알림, 추천 종목 알림, 실시간 알림 발송

<details>
<summary>구조 보기</summary>

```
notification/
├── api/
│   └── NotificationController.java
├── model/
│   └── NotificationResponse.java
├── application/
│   └── NotificationApplicationService.java
├── domain/
│   └── Notification.java
├── repository/
│   └── NotificationRepository.java
└── infrastructure/
    └── JpaNotificationRepository.java
```

</details>

---

## 🏛️ 아키텍처

### Layered Architecture

```
┌─────────────────────────────────────┐
│   Presentation Layer (API)          │
│        *Controller.java             │
└──────────────┬──────────────────────┘
               │ depends on ↓
┌──────────────▼──────────────────────┐
│   Application Layer (Service)       │
│    *ApplicationService.java         │
└──────────────┬──────────────────────┘
               │ depends on ↓
┌──────────────▼──────────────────────┐
│      Domain Layer (Core)            │
│  집합체 루트 / 엔티티 / 값 객체      │
│      ⚠️ 외부 의존성 없음             │
└──────────────▲──────────────────────┘
               │ implements ↑
┌──────────────┴──────────────────────┐
│  Infrastructure Layer (Repository)  │
│       Jpa*Repository.java           │
└─────────────────────────────────────┘
```

### 핵심 DDD 패턴

- **Aggregate Root**: 데이터 변경의 일관성 경계
- **Entity**: 식별자 기반 동등성 비교
- **Value Object**: 값 기반 동등성 비교, 불변 객체
- **Repository Pattern**: 도메인과 인프라 분리
- **Domain Event**: 비즈니스 이벤트 발행

---

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.1.9
- **Language**: Java 17
- **Build Tool**: Gradle
- **Security**: Spring Security + JWT
- **Database**: MySQL, Redis, Elasticsearch
- **ORM**: Spring Data JPA

### Architecture
- DDD (Domain-Driven Design)
- Layered Architecture
- Repository Pattern
- CQRS (Command Query Responsibility Segregation)

---

## 🚀 시작하기

### 필수 요구사항

- JDK 17 이상
- Gradle 7.x 이상
- MySQL 8.0
- Redis 7.x

### 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 도커 컴포즈로 실행
docker-compose up -d
```

---

## 📋 모듈 간 의존성

```
application (메인)
  ├── user
  ├── common
  └── all modules

user, master, stock, notification
  └── common

analysis
  ├── common
  ├── master
  └── stock

portfolio
  ├── common
  └── stock

question
  ├── common
  └── master
```

---

## ✅ 구현 상태

### 완료
- ✨ JWT 기반 인증/인가
- 📧 이메일 인증 시스템
- 🔐 소셜 로그인 (카카오)
- 👤 사용자 관리
- 🏗️ 멀티모듈 구조
- 🎯 DDD 기반 설계

### 개발 예정
- 📈 종목 상세 분석
- 🎓 거장 분석 로직
- 💼 포트폴리오 관리
- ❓ RAG 기반 질문 시스템
- 🔔 실시간 알림

