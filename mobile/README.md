# 📱 Point Roulette - Mobile

Point Roulette 서비스의 모바일 앱 프로젝트입니다. Flutter를 사용하여 크로스 플랫폼(Android, iOS)을 지원하며, 웹뷰(WebView) 기반의 하이브리드 앱으로 구현되었습니다. 웹 프론트엔드(`frontend`)를 네이티브 앱 환경에서 구동하기 위한 래퍼(Wrapper) 앱이며, 기본적인 웹뷰 기능 외에도 네이티브 경험을 향상시키기 위한 다양한 부가 기능이 포함되어 있습니다.

## 📑 목차

- [🛠 기술 스택 (Tech Stack)](#-기술-스택-tech-stack)
- [✨ 주요 기능 (Key Features)](#-주요-기능-key-features)
- [🚀 시작하기 (Getting Started)](#-시작하기-getting-started)
- [📂 프로젝트 구조 (Project Structure)](#-프로젝트-구조-project-structure)

---

## 🛠 기술 스택 (Tech Stack)

- **Framework**: Flutter (Dart)
- **WebView**: `webview_flutter` ^4.8.0
- **Assets Management**:
  - `flutter_launcher_icons`: 앱 아이콘 자동 생성
  - `flutter_native_splash`: 네이티브 스플래시 스크린 생성

## ✨ 주요 기능 (Key Features)

### 1. 웹뷰 (WebView) 통합
- **URL**: `https://assignment-nine-lemon.vercel.app/home`
- **JavaScript Interface**: Alert, Confirm, Prompt 등의 브라우저 다이얼로그를 네이티브 UI(`AlertDialog`)로 매핑하여 일관된 사용자 경험 제공
- **Navigation**: 안드로이드 뒤로가기 버튼(Physical Back Button) 핸들링 및 히스토리 관리

### 2. 향상된 사용자 경험 (UX)

<img width="330" height="692" alt="스크린샷 2026-02-06 오후 4 32 17" src="https://github.com/user-attachments/assets/36da8677-2059-4a83-bd8a-63ec70eaaddf" />
<img width="320" height="686" alt="스크린샷 2026-02-06 오후 4 32 08" src="https://github.com/user-attachments/assets/d6f7044d-3ba3-4a39-8eab-3bcd60dd0d5c" />

- **스플래시 스크린**: 앱 시작 시 브랜드 아이덴티티를 보여주는 네이티브 스플래시 적용
- **로딩 인디케이터**: 웹 페이지 로딩 중 네이티브 스피너(Loading Spinner) 표시로 대기 시간 인지 제공

### 3. 에러 핸들링
<img width="334" height="693" alt="스크린샷 2026-02-06 오후 4 32 28" src="https://github.com/user-attachments/assets/196d0fae-795a-4243-a5e8-9366f6af433f" />

- **네트워크 연결 상태 감지**: 오프라인 상태이거나 리소스 로딩 실패 시 커스텀 에러 페이지 노출
- **재시도 메커니즘**: 에러 화면에서 '재시도' 버튼을 통해 페이지 리로드 지원

## 🚀 시작하기 (Getting Started)

### 1. 사전 요구사항 (Prerequisites)
- Flutter SDK (3.10.8 이상)
- Android Studio / Xcode

### 2. 의존성 설치 (Install Dependencies)
```bash
flutter pub get
```

### 3. 앱 실행 (Run App)
```bash
# Android
flutter run -d android

# iOS
flutter run -d ios
```

### 4. 아이콘 및 스플래시 재생성 (Regenerate Assets)
이미지 리소스 변경 시 아래 명령어로 네이티브 설정을 갱신할 수 있습니다.
```bash
# 앱 아이콘 재생성
flutter pub run flutter_launcher_icons

# 스플래시 스크린 재생성
flutter pub run flutter_native_splash:create
```

## 📂 프로젝트 구조 (Project Structure)

```
lib/
├── main.dart             # 앱 진입점 및 메인 로직
└── ...
assets/
└── icon.png              # 앱 아이콘 및 스플래시 이미지 소스
```
