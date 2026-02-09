# 🎰 Point Roulette (포인트 룰렛)

매일 룰렛을 돌려 포인트를 획득하고, 획득한 포인트로 상품을 구매하는 서비스입니다. AI를 적극 활용하여 포인트 기반 이벤트 서비스를 풀스택으로 구현하고 배포했습니다.

## 📝 제출 정보 (Submission)

### 1. 배포 URL (Deployment)
- **Frontend**: [\[Vercel Deployment URL\]](https://assignment-nine-lemon.vercel.app)
- **Admin**: [\[Admin Deployment URL\]](https://assignment-fexi.vercel.app/)
- **Backend API (Swagger)**: [\[Swagger UI URL\]](https://assignment-ybpt.onrender.com/swagger-ui/index.html)

### 2. 테스트 계정 (Test Account)
```
닉네임: testuser
```
> 로그인 시 닉네임만 입력하면 자동으로 계정이 생성됩니다.

### 3. 모바일 앱 (Mobile App)
- **APK Download**: [\[Google Drive\]](https://drive.google.com/file/d/14BnWyNXuwlHFgSxJJyPmmTKbtdwiokff/view?usp=sharing)

### 4. CI/CD 파이프라인
백엔드 배포 자동화가 GitHub Actions로 구축되어 있습니다.
- Render를 활용하여 배포를 진행합니다.
- Github Actions를 활용하여 CI를 진행합니다.
- 설정 파일: `.github/workflows/backend-ci.yml` 

### 5. AI 활용 리포트 (AI Collaboration)
개발 과정에서의 AI 협업 프롬프트 기록입니다.
- **Report**: [PROMPT.md](./PROMPT.md)

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 스택 |
|------|-----------|
| **Backend** | Kotlin, Spring Boot 3, JPA, PostgreSQL |
| **Frontend** | React 19, TypeScript, Vite, TailwindCSS |
| **Admin** | React 18, TypeScript, Ant Design |
| **Mobile** | Flutter, WebView (Wrapper App) |
| **Infra** | Vercel, Render, Neon DB, GitHub Actions |

## 📦 구성 요소 (Components)

각 패키지별 상세 내용은 아래 링크에서 확인할 수 있습니다.

| 패키지 | 설명 | 바로가기 |
|--------|------|----------|
| **Backend** | API 서버, DB, 인증, 포인트 시스템 | [Backend README](./backend/README.md) |
| **Frontend** | 사용자용 웹 서비스 (모바일 뷰 최적화) | [Frontend README](./frontend/README.md) |
| **Admin** | 운영자용 대시보드 및 관리 시스템 | [Admin README](./admin/README.md) |
| **Mobile** | 사용자용 Flutter 하이브리드 앱 | [Mobile README](./mobile/README.md) |

## ✨ 주요 기능 (Key Features)

- **일일 예산 관리**: 하루 총 100,000p 예산 내에서 랜덤 (100 ~ 1000p) 지급, 소진 시 자동 차단
- **1일 1회 참여**: 유저별 하루 1회 참여 제한
- **포인트 시스템**: 획득, 사용, 만료(30일), 만료 예정 알림(7일 전)
- **상품 구매**: 포인트로 경품 응모 및 구매
- **어드민 관리**: 대시보드, 예산 설정, 주문/참여 취소 및 환불

## 🚀 시작하기 (Getting Started)

각 프로젝트 폴더 내 `README.md`에 상세한 실행 방법이 기술되어 있습니다.

### 1. 백엔드 실행 (Backend)
```bash
cd backend
./gradlew bootRun
```

### 2. 프론트엔드 실행 (Frontend)
```bash
cd frontend
npm install
npm run dev
```

### 3. 어드민 실행 (Admin)
```bash
cd admin
npm install
npm run dev
```

### 4. 모바일 앱 실행 (Mobile)
```bash
cd mobile
flutter run
```

## 📂 프로젝트 구조 (Project Structure)

```
.
├── admin/           # 어드민 웹 프로젝트
├── backend/         # 백엔드 API 프로젝트
├── frontend/        # 사용자 웹 프로젝트
├── mobile/          # 모바일 앱 프로젝트
├── API.md           # API 명세 문서
├── PROMPT.md        # AI 프롬프트 기록
└── README.md        # 프로젝트 메인 문서
```
