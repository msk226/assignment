import axios from 'axios';

// VITE_API_URL이 없으면 기본적으로 localhost:8080을 사용합니다.
const baseURL = import.meta.env.VITE_API_URL || 'https://assignment-ybpt.onrender.com/';

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키 기반 인증을 위해 필요할 수 있습니다.
});

// 응답 인터셉터 (에러 처리 공통화 등)
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // 공통 에러 처리 로직 (예: 401 시 로그인 페이지로 리다이렉트 등)
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);
