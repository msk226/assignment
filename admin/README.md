# 🎲 Point Roulette Admin

포인트 룰렛 이벤트 서비스를 운영하기 위한 관리자 대시보드입니다.
이 어드민 페이지를 통해 **일일 예산 관리, 상품 CRUD, 주문 취소/환불, 룰렛 참여 취소** 등의 운영 업무를 수행할 수 있습니다.

## 🛠 기술 스택 (Tech Stack)

- **Core**: React 18, TypeScript, Vite
- **State Management**: TanStack Query (v5)
- **UI Library**: Ant Design (antd)
- **HTTP Client**: Axios
- **Routing**: React Router DOM (v6)

## ✨ 주요 기능 (Key Features)

| 메뉴 | 기능 상세 |
|------|-----------|
| **대시보드** | 오늘의 총 예산, 소진된 예산, 잔여 예산, 참여자 수, 총 지급 포인트 현황 실시간 모니터링 |
| **예산 관리** | 날짜별 일일 예산 설정 및 조회. (필수: 예산 초과 방지 운영을 위한 핵심 기능) |
| **상품 관리** | 경품 상품 목록 조회, 상품 추가, 수정, 삭제(CRUD) 및 재고 관리 |
| **주문 내역** | 사용자 상품 구매 내역 조회. **주문 취소 기능**을 통해 포인트 환불 처리 가능 |
| **룰렛 관리** | 사용자들의 룰렛 참여 기록 조회. 부정 참여 등의 사유로 **참여 취소 시 포인트 회수** 가능 |

## 🚀 시작하기 (Getting Started)

### 1. 프로젝트 설치 (Installation)

```bash
cd admin
npm install
```

### 2. 환경 변수 설정 (Environment Setup)

프로젝트 루트에 `.env` 파일을 생성하고 백엔드 API 주소를 설정할 수 있습니다.
(설정하지 않을 경우 기본값으로 프로덕션 서버 또는 로컬 기본값을 사용하도록 코드에 설정되어 있습니다.)

```env
VITE_API_URL=http://localhost:8080/
```

### 3. 개발 서버 실행 (Run Dev Server)

```bash
npm run dev
```

브라우저에서 `http://localhost:5173` 으로 접속합니다.

### 4. 빌드 및 배포 (Build & Deploy)

```bash
npm run build
```

빌드 결과물은 `dist` 디렉토리에 생성됩니다. Vercel 등의 배포 플랫폼을 통해 손쉽게 배포할 수 있습니다.

## 📂 프로젝트 구조 (Project Structure)

```
src/
├── api/          # API 연동 (endpoints.ts, client.ts)
├── components/   # 공통 컴포넌트 (AdminLayout 등)
├── pages/        # 페이지별 컴포넌트
│   ├── Dashboard.tsx  # 대시보드
│   ├── Budget.tsx     # 예산 관리
│   ├── Products.tsx   # 상품 관리
│   ├── Orders.tsx     # 주문 내역 및 취소
│   └── Roulette.tsx   # 룰렛 참여 내역 및 취소
├── types/        # TypeScript 타입 정의
├── App.tsx       # 라우트 정의
└── main.tsx      # Entry Point
```
