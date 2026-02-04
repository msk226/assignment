# Point Roulette Design System (VoltUp Theme)

이 문서는 Point Roulette 서비스의 디자인 시스템을 정의합니다. 어드민 페이지 및 추후 확장되는 모든 서비스는 이 가이드를 준수하여 통일된 브랜드 경험을 제공해야 합니다.

## 🎨 Color Palette (색상 팔레트)

VoltUp의 브랜드 아이덴티티는 **"Neon Lime & Deep Black"**의 강렬한 대비를 기반으로 합니다.

| Role | Color Name | Hex Code | Description |
|---|---|---|---|
| **Primary** | **Neon Lime** | `#D9FD01` | 브랜드 메인 컬러. 강조, 액션 버튼, 배지 배경 등에 사용 |
| **Primary FG** | **Deep Black** | `#1A1A1A` | Primary 배경 위의 텍스트/아이콘 색상 (가독성 최적화) |
| **Background** | **White** | `#FFFFFF` | 기본 페이지 배경 |
| **Surface** | **Gray 50** | `#F9FAFB` | 섹션 배경, 모바일 레이아웃 컨테이너 배경 |
| **Neutral** | **Gray 900** | `#111827` | 기본 본문 텍스트, 강렬한 대비가 필요한 배경 (게이지 트랙 등) |

## 🔠 Typography (타이포그래피)

- **Font Family**: 시스템 기본 산세리프 폰트 (Apple SD Gothic Neo, Pretendard, sans-serif 권장)
- **Weight**:
    - **Bold (700)**: 헤더, 포인트 숫자, 강조 텍스트
    - **Medium (500)**: 보조 레이블, 본문
- **Readable Text**:
    - 흰 배경 위에서는 `text-gray-900` (진한 회색/검정)을 사용하여 가독성을 확보합니다.
    - `text-gray-500` 등 연한 회색은 보조 정보에만 제한적으로 사용합니다.

## 🧩 UI Components (UI 컴포넌트)

### 1. High Contrast Badge (강조 배지)
데이터(포인트, 금액 등)를 표시할 때 사용하는 핵심 컴포넌트입니다.

- **Style**: `bg-primary` (Lime) + `text-gray-900` (Black)
- **Shape**: `rounded-full` (완전 둥근 형태) 또는 `rounded-lg` (둥근 사각형)
- **Usage**:
    - 보유 포인트, 상품 가격, 주문 금액
    - 헤더의 서비스 타이틀

```tsx
// 예시 코드 (Tailwind CSS)
<span className="bg-[#D9FD01] text-[#1A1A1A] px-3 py-1 rounded-full font-bold text-sm">
  1,000 P
</span>
```

### 2. Header Identity (헤더 아이덴티티)
서비스 로고와 타이틀을 하나로 묶어 브랜드 존재감을 높입니다.

- **Layout**: 로고(심볼) + 텍스트 배지 결합
- **Text Badge**: `bg-primary` 배경 안에 서비스명 표시

### 3. Neon Gauge (네온 게이지)
진척도나 예산을 표시할 때 형광등처럼 빛나는 효과를 줍니다.

- **Track (배경)**: `bg-gray-900` (진한 검정)
- **Indicator (채움)**: `bg-primary` (라임)
- **Effect**: 어두운 트랙 위에서 라임색이 더욱 선명하게 부각됨

```tsx
<div className="w-full bg-gray-900 rounded-full h-2.5">
  <div className="bg-[#D9FD01] h-2.5 rounded-full" style={{ width: '50%' }} />
</div>
```

### 4. Icons (아이콘)
아이콘은 단순한 장식이 아닌 브랜드 요소로 활용합니다.

- **Style**: `bg-primary` (Lime) 배경 + `text-gray-900` (Black) 아이콘
- **Shape**: `rounded-lg` 또는 `rounded-full`

---
*Created by Antigravity for Point Roulette Project*
