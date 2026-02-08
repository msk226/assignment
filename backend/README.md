# 🔋 Point Roulette - Backend

매일 룰렛을 돌려 포인트를 획득하고, 획득한 포인트로 상품을 구매하는 포인트 룰렛 서비스의 백엔드 입니다.

## 📑 목차

- [🛠 기술 스택 (Tech Stack)](#-기술-스택-tech-stack)
- [✨ 주요 기능 (Key Features)](#-주요-기능-key-features)
- [🚀 시작하기 (Getting Started)](#-시작하기-getting-started)
- [핵심 설계 (Core Design)](#핵심-설계-core-design)
- [📂 프로젝트 구조 (Project Structure)](#-프로젝트-구조-project-structure)

---

## 🛠 기술 스택 (Tech Stack)

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.3.5
- **JDK**: Java 21
- **ORM**: Spring Data JPA
- **Database**: H2 (개발) / PostgreSQL (운영)
- **API 문서**: SpringDoc OpenAPI 3 (Swagger UI)
- **빌드**: Gradle (Kotlin DSL)
- **코드 품질**: detekt, ktlint

## ✨ 주요 기능 (Key Features)

### 인증

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/login` | 로그인 (닉네임 입력, 미가입 시 자동 생성) |
| GET | `/api/auth/me` | 내 정보 조회 |

> 인증은 `X-User-Id` 헤더로 처리합니다. 로그인 시 반환되는 `userId`를 이후 요청의 헤더에 포함합니다.

### 룰렛

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/roulette/spin` | 룰렛 참여 (1일 1회, 100~1,000p 랜덤) |
| GET | `/api/roulette/status` | 오늘 참여 여부 및 잔여 예산 조회 |
| GET | `/api/roulette/history` | 내 룰렛 참여 내역 조회 |

### 포인트

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/points` | 내 포인트 목록 (유효기간 포함, status 필터 가능) |
| GET | `/api/points/balance` | 잔액 조회 (총 잔액 + 7일 내 만료 예정) |
| GET | `/api/points/expiring` | 7일 내 만료 예정 포인트 상세 조회 |

### 상품

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/products` | 활성 상품 목록 조회 |
| GET | `/api/products/{id}` | 상품 상세 조회 |

### 주문

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/orders` | 상품 주문 (포인트 차감) |
| GET | `/api/orders` | 내 주문 내역 조회 |

### 어드민

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/admin/dashboard` | 대시보드 (예산 현황, 참여자 수, 지급 포인트) |
| GET | `/api/admin/budget` | 일일 예산 조회 |
| PUT | `/api/admin/budget` | 일일 예산 설정 |
| GET | `/api/admin/roulette` | 오늘 룰렛 참여 목록 |
| POST | `/api/admin/roulette/{id}/cancel` | 룰렛 참여 취소 (포인트 회수) |
| GET | `/api/admin/products` | 전체 상품 목록 (비활성 포함) |
| POST | `/api/admin/products` | 상품 등록 |
| PUT | `/api/admin/products/{id}` | 상품 수정 |
| DELETE | `/api/admin/products/{id}` | 상품 비활성화 |
| GET | `/api/admin/orders` | 전체 주문 목록 |
| DELETE | `/api/admin/orders/{id}` | 주문 취소 (포인트 환불) |

## 🚀 시작하기 (Getting Started)

### 1. 로컬 개발 환경 (Local Development)

```bash
# 빌드 및 실행
./gradlew bootRun

# 테스트 실행
./gradlew test

# 린트 체크
./gradlew ktlintCheck detekt
```

실행 후 접속:
- API 서버: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- H2 콘솔: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)

### 2. Docker 실행 (Docker)

```bash
docker build -t voltup-backend .
docker run -p 10000:10000 \
  -e DATABASE_URL=jdbc:postgresql://host:5432/dbname?user=xxx&password=xxx \
  voltup-backend
```

## 핵심 설계 (Core Design)

### 동시성 제어

다수의 유저가 동시에 룰렛에 참여하거나 상품을 구매할 때 데이터 정합성을 보장합니다.

- **비관적 락(Pessimistic Lock)**: 예산, 상품 재고, 포인트 차감 시 `PESSIMISTIC_WRITE` 락 적용 (3초 타임아웃)
- **DB Unique Constraint**: `roulette_participations` 테이블에 `(user_id, date)` 유니크 제약으로 중복 참여 원천 차단
- **락 획득 순서 통일**: 룰렛 참여 시 예산 락 → 참여 여부 체크 → 포인트 지급 순서로 데드락 방지

### 포인트 시스템

- **유효기간**: 획득일로부터 30일
- **FIFO 차감**: 상품 구매 시 만료일이 가까운 포인트부터 우선 차감
- **상태 관리**: EARNED / EXPIRED / CANCELED (effectiveStatus로 만료 자동 판별)
- **참여-포인트 연관**: `participationId`로 룰렛 참여와 포인트를 직접 매핑하여 정확한 취소 처리

### 일일 예산

- 기본 100,000p, 어드민이 변경 가능
- 예산 소진 시 참여 불가 (100p 미만 잔여 시에도 불가)
- 참여 취소 시 당일이면 예산 복구

### 주문 취소

- 어드민이 주문 취소 시 포인트 환불 + 상품 재고 복구
- 환불 포인트는 새 포인트로 생성 (30일 유효기간 부여)

## 📂 프로젝트 구조 (Project Structure)

```
src/main/kotlin/lg/voltup/
├── config/          # CORS, Swagger 설정
├── controller/      # REST API 엔드포인트
│   └── dto/         # 요청/응답 DTO
├── entity/          # JPA 엔티티
│   └── enums/       # 상태 열거형 (OrderStatus, PointStatus, ParticipationStatus)
├── exception/       # 커스텀 예외 및 GlobalExceptionHandler
├── repository/      # 데이터 접근 계층
├── service/         # 비즈니스 로직
└── BackendApplication.kt
```
