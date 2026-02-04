# Point Roulette API 명세서

## 개요

- **Base URL**: `http://localhost:8080` (개발) / `https://your-api-domain.com` (운영)
- **인증 방식**: `X-User-Id` 헤더에 사용자 ID 전달
- **Content-Type**: `application/json`

## 공통 사항

### 인증 헤더

로그인 후 받은 `userId`를 모든 API 요청의 헤더에 포함해야 합니다.

```
X-User-Id: {userId}
```

### 에러 응답 형식

```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "에러 메시지"
}
```

### 주요 에러 코드

| 코드 | 설명 |
|------|------|
| 400 | 잘못된 요청 (유효성 검사 실패, 비즈니스 로직 오류) |
| 404 | 리소스를 찾을 수 없음 |
| 409 | 충돌 (중복 참여 등) |
| 500 | 서버 내부 오류 |

---

## 1. 인증 API

### 1.1 로그인

닉네임으로 로그인합니다. 존재하지 않는 닉네임이면 자동으로 계정이 생성됩니다.

```
POST /api/auth/login
```

**Request Body**
```json
{
  "nickname": "string"
}
```

**Response** `200 OK`
```json
{
  "userId": 1,
  "nickname": "닉네임"
}
```

---

### 1.2 내 정보 조회

현재 로그인한 유저 정보를 조회합니다.

```
GET /api/auth/me
```

**Headers**
```
X-User-Id: {userId}
```

**Response** `200 OK`
```json
{
  "id": 1,
  "nickname": "닉네임"
}
```

---

## 2. 룰렛 API

### 2.1 룰렛 참여

룰렛을 돌려 포인트를 획득합니다.

```
POST /api/roulette/spin
```

**Headers**
```
X-User-Id: {userId}
```

**Response** `200 OK`
```json
{
  "points": 350,
  "remainingBudget": 99650,
  "message": "350p를 획득했습니다!"
}
```

**에러 응답**

| 상황 | 코드 | 메시지 |
|------|------|--------|
| 이미 참여함 | 400 | "오늘 이미 참여했습니다." |
| 예산 소진 | 400 | "오늘 예산이 소진되었습니다." |

---

### 2.2 룰렛 상태 조회

오늘 참여 여부와 잔여 예산을 조회합니다.

```
GET /api/roulette/status
```

**Headers**
```
X-User-Id: {userId}
```

**Response** `200 OK`
```json
{
  "hasParticipatedToday": false,
  "todayPoints": null,
  "remainingBudget": 100000,
  "totalBudget": 100000
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `hasParticipatedToday` | boolean | 오늘 참여 여부 |
| `todayPoints` | number \| null | 오늘 획득한 포인트 (미참여 시 null) |
| `remainingBudget` | number | 오늘 잔여 예산 |
| `totalBudget` | number | 오늘 총 예산 |

---

## 3. 포인트 API

### 3.1 내 포인트 목록

보유 포인트 목록을 조회합니다. (유효기간 포함)

```
GET /api/points
```

**Headers**
```
X-User-Id: {userId}
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "amount": 350,
    "usedAmount": 0,
    "availableAmount": 350,
    "earnedAt": "2024-01-01T12:00:00",
    "expiresAt": "2024-01-31T12:00:00",
    "isExpired": false,
    "daysUntilExpiry": 30
  }
]
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `amount` | number | 획득한 총 포인트 |
| `usedAmount` | number | 사용한 포인트 |
| `availableAmount` | number | 사용 가능한 포인트 |
| `isExpired` | boolean | 만료 여부 |
| `daysUntilExpiry` | number | 만료까지 남은 일수 |

---

### 3.2 포인트 잔액 조회

사용 가능한 총 포인트 잔액을 조회합니다.

```
GET /api/points/balance
```

**Headers**
```
X-User-Id: {userId}
```

**Response** `200 OK`
```json
{
  "totalBalance": 5000,
  "expiringWithin7Days": 350
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `totalBalance` | number | 총 사용 가능 포인트 |
| `expiringWithin7Days` | number | 7일 내 만료 예정 포인트 |

---

### 3.3 만료 예정 포인트 조회

7일 내 만료 예정인 포인트를 조회합니다.

```
GET /api/points/expiring
```

**Headers**
```
X-User-Id: {userId}
```

**Response** `200 OK`
```json
{
  "points": [
    {
      "id": 1,
      "amount": 350,
      "usedAmount": 0,
      "availableAmount": 350,
      "earnedAt": "2024-01-01T12:00:00",
      "expiresAt": "2024-01-08T12:00:00",
      "isExpired": false,
      "daysUntilExpiry": 5
    }
  ],
  "totalExpiringAmount": 350
}
```

---

## 4. 상품 API (사용자)

### 4.1 상품 목록 조회

판매 중인 상품 목록을 조회합니다.

```
GET /api/products
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "name": "상품명",
    "description": "상품 설명",
    "price": 1000,
    "stock": 100,
    "imageUrl": "https://example.com/image.jpg",
    "isActive": true,
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

---

### 4.2 상품 상세 조회

상품 상세 정보를 조회합니다.

```
GET /api/products/{productId}
```

**Response** `200 OK`
```json
{
  "id": 1,
  "name": "상품명",
  "description": "상품 설명",
  "price": 1000,
  "stock": 100,
  "imageUrl": "https://example.com/image.jpg",
  "isActive": true,
  "createdAt": "2024-01-01T12:00:00"
}
```

---

## 5. 주문 API (사용자)

### 5.1 상품 주문

포인트를 사용하여 상품을 주문합니다.

```
POST /api/orders
```

**Headers**
```
X-User-Id: {userId}
```

**Request Body**
```json
{
  "productId": 1
}
```

**Response** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "productId": 1,
  "productName": "상품명",
  "pointsUsed": 1000,
  "status": "COMPLETED",
  "createdAt": "2024-01-01T12:00:00"
}
```

**에러 응답**

| 상황 | 코드 | 메시지 |
|------|------|--------|
| 상품 없음 | 404 | "상품을 찾을 수 없습니다." |
| 포인트 부족 | 400 | "포인트가 부족합니다." |
| 재고 부족 | 400 | "재고가 부족합니다." |
| 판매 중지 | 400 | "판매 중인 상품이 아닙니다." |

---

### 5.2 내 주문 내역 조회

내 주문 내역을 조회합니다.

```
GET /api/orders
```

**Headers**
```
X-User-Id: {userId}
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "productId": 1,
    "productName": "상품명",
    "pointsUsed": 1000,
    "status": "COMPLETED",
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

| status 값 | 설명 |
|-----------|------|
| `COMPLETED` | 주문 완료 |
| `CANCELLED` | 주문 취소 |

---

## 6. 어드민 API

> 어드민 API는 관리자 전용입니다.

### 6.1 대시보드 조회

오늘의 예산 현황, 참여자 수, 지급 포인트를 조회합니다.

```
GET /api/admin/dashboard
```

**Response** `200 OK`
```json
{
  "date": "2024-01-01",
  "totalBudget": 100000,
  "usedBudget": 35000,
  "remainingBudget": 65000,
  "participantCount": 50,
  "totalPointsDistributed": 35000
}
```

---

### 6.2 예산 조회

오늘의 일일 예산을 조회합니다.

```
GET /api/admin/budget
```

**Response** `200 OK`
```json
{
  "date": "2024-01-01",
  "totalBudget": 100000,
  "usedBudget": 35000,
  "remainingBudget": 65000
}
```

---

### 6.3 예산 설정

오늘의 일일 예산을 설정합니다.

```
PUT /api/admin/budget
```

**Request Body**
```json
{
  "totalBudget": 150000
}
```

**Response** `200 OK`
```json
{
  "date": "2024-01-01",
  "totalBudget": 150000,
  "usedBudget": 35000,
  "remainingBudget": 115000
}
```

**에러 응답**

| 상황 | 코드 | 메시지 |
|------|------|--------|
| 사용 예산보다 작게 설정 | 400 | "새 예산은 이미 사용된 예산보다 작을 수 없습니다." |

---

### 6.4 룰렛 참여 목록 조회

오늘의 룰렛 참여 목록을 조회합니다.

```
GET /api/admin/roulette
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "nickname": "닉네임",
    "points": 350,
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

---

### 6.5 룰렛 참여 취소

룰렛 참여를 취소하고 포인트를 회수합니다.

```
DELETE /api/admin/roulette/{participationId}
```

**Response** `204 No Content`

---

### 6.6 상품 목록 조회 (어드민)

전체 상품 목록을 조회합니다. (비활성 상품 포함)

```
GET /api/admin/products
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "name": "상품명",
    "description": "상품 설명",
    "price": 1000,
    "stock": 100,
    "imageUrl": "https://example.com/image.jpg",
    "isActive": true,
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

---

### 6.7 상품 등록

새 상품을 등록합니다.

```
POST /api/admin/products
```

**Request Body**
```json
{
  "name": "상품명",
  "description": "상품 설명 (선택)",
  "price": 1000,
  "stock": 100,
  "imageUrl": "https://example.com/image.jpg (선택)"
}
```

| 필드 | 필수 | 타입 | 설명 |
|------|------|------|------|
| `name` | O | string | 상품명 |
| `description` | X | string | 상품 설명 |
| `price` | O | number | 가격 (포인트) |
| `stock` | X | number | 재고 (기본값: 0) |
| `imageUrl` | X | string | 이미지 URL |

**Response** `200 OK`
```json
{
  "id": 1,
  "name": "상품명",
  "description": "상품 설명",
  "price": 1000,
  "stock": 100,
  "imageUrl": "https://example.com/image.jpg",
  "isActive": true,
  "createdAt": "2024-01-01T12:00:00"
}
```

---

### 6.8 상품 수정

상품 정보를 수정합니다. 변경할 필드만 전달합니다.

```
PUT /api/admin/products/{productId}
```

**Request Body**
```json
{
  "name": "새 상품명 (선택)",
  "description": "새 설명 (선택)",
  "price": 1500,
  "stock": 50,
  "imageUrl": "https://example.com/new-image.jpg (선택)",
  "isActive": false
}
```

| 필드 | 필수 | 타입 | 설명 |
|------|------|------|------|
| `name` | X | string | 상품명 |
| `description` | X | string | 상품 설명 |
| `price` | X | number | 가격 |
| `stock` | X | number | 재고 |
| `imageUrl` | X | string | 이미지 URL |
| `isActive` | X | boolean | 판매 활성화 여부 |

**Response** `200 OK`

---

### 6.9 상품 삭제

상품을 비활성화합니다. (실제로 삭제되지 않고 `isActive`가 `false`로 변경됨)

```
DELETE /api/admin/products/{productId}
```

**Response** `204 No Content`

---

### 6.10 전체 주문 목록 조회

전체 주문 목록을 조회합니다.

```
GET /api/admin/orders
```

**Response** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "productId": 1,
    "productName": "상품명",
    "pointsUsed": 1000,
    "status": "COMPLETED",
    "createdAt": "2024-01-01T12:00:00"
  }
]
```

---

### 6.11 주문 취소

주문을 취소하고 포인트를 환불합니다.

```
DELETE /api/admin/orders/{orderId}
```

**Response** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "productId": 1,
  "productName": "상품명",
  "pointsUsed": 1000,
  "status": "CANCELLED",
  "createdAt": "2024-01-01T12:00:00"
}
```

**에러 응답**

| 상황 | 코드 | 메시지 |
|------|------|--------|
| 주문 없음 | 404 | "주문을 찾을 수 없습니다." |
| 이미 취소됨 | 400 | "이미 취소된 주문입니다." |

---

## API 요약

### 사용자 API

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/login` | 로그인 | X |
| GET | `/api/auth/me` | 내 정보 | O |
| POST | `/api/roulette/spin` | 룰렛 참여 | O |
| GET | `/api/roulette/status` | 룰렛 상태 | O |
| GET | `/api/points` | 포인트 목록 | O |
| GET | `/api/points/balance` | 포인트 잔액 | O |
| GET | `/api/points/expiring` | 만료 예정 포인트 | O |
| GET | `/api/products` | 상품 목록 | X |
| GET | `/api/products/{id}` | 상품 상세 | X |
| POST | `/api/orders` | 주문 생성 | O |
| GET | `/api/orders` | 내 주문 내역 | O |

### 어드민 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/admin/dashboard` | 대시보드 |
| GET | `/api/admin/budget` | 예산 조회 |
| PUT | `/api/admin/budget` | 예산 설정 |
| GET | `/api/admin/roulette` | 참여 목록 |
| DELETE | `/api/admin/roulette/{id}` | 참여 취소 |
| GET | `/api/admin/products` | 상품 목록 |
| POST | `/api/admin/products` | 상품 등록 |
| PUT | `/api/admin/products/{id}` | 상품 수정 |
| DELETE | `/api/admin/products/{id}` | 상품 삭제 |
| GET | `/api/admin/orders` | 주문 목록 |
| DELETE | `/api/admin/orders/{id}` | 주문 취소 |

---

## Swagger UI

API 문서 및 테스트는 Swagger UI에서 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```
