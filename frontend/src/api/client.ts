import axios from 'axios';

// 백엔드 API URL 설정 (환경 변수 또는 기본값)
const baseURL = import.meta.env.VITE_API_URL || 'https://assignment-ybpt.onrender.com/';

export const apiClient = axios.create({
    baseURL,
    headers: {
        'Content-Type': 'application/json',
    },
    // 쿠키 기반 인증이 필요할 경우 사용 (현재 프로젝트는 Header 기반이지만 WebView 등 고려하여 true 설정)
    withCredentials: true,
    timeout: 15000, // 15초 타임아웃
});

// 요청 인터셉터: X-User-Id 헤더 자동 추가
apiClient.interceptors.request.use(
    (config) => {
        const userId = localStorage.getItem('userId');
        if (userId) {
            config.headers['X-User-Id'] = userId;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('API Error:', error);
        return Promise.reject(error);
    }
);
