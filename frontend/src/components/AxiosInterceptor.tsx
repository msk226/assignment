import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { apiClient } from '../api/client';
import axios from 'axios';

export const AxiosInterceptor = () => {
    const navigate = useNavigate();
    const location = useLocation();

    useEffect(() => {
        const interceptor = apiClient.interceptors.response.use(
            (response) => response,
            (error) => {
                if (axios.isAxiosError(error)) {
                    // Check for timeout or network error
                    if (error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK') {
                        const currentPath = location.pathname + location.search;
                        // Avoid redirect loops if already on network error page
                        if (!currentPath.includes('/network-error')) {
                            navigate(`/network-error?from=${encodeURIComponent(currentPath)}`);
                        }
                    }
                }
                return Promise.reject(error);
            }
        );

        return () => {
            apiClient.interceptors.response.eject(interceptor);
        };
    }, [navigate, location]);

    return null;
};
