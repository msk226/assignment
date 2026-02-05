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
    // 서버에서 보내주는 에러 메시지 추출
    const serverErrorMessage = error.response?.data?.message || '알 수 없는 에러가 발생했습니다.';

    // 에러 객체에 message 속성을 덮어쓰거나, 새로운 속성으로 추가하여 UI에서 사용할 수 있게 함
    error.message = serverErrorMessage;

    console.error('API Error:', serverErrorMessage, error);
    return Promise.reject(error);
  }
);
