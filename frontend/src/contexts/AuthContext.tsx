import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiClient } from '../api/client';

interface User {
    id: number;
    nickname: string;
}

interface AuthContextType {
    user: User | null;
    login: (nickname: string) => Promise<void>;
    logout: () => void;
    isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        // 앱 시작 시 로컬 스토리지에서 유저 정보 복원
        const storedUserId = localStorage.getItem('userId');
        const storedNickname = localStorage.getItem('nickname');

        if (storedUserId && storedNickname) {
            setUser({
                id: parseInt(storedUserId),
                nickname: storedNickname,
            });
        }
        setIsLoading(false);
    }, []);

    const login = async (nickname: string) => {
        try {
            // Mock Login API 호출
            const response = await apiClient.post('/api/auth/login', { nickname });
            const userData = response.data; // { userId: 1, nickname: "닉네임" }

            const user: User = {
                id: userData.userId,
                nickname: userData.nickname,
            };

            setUser(user);
            localStorage.setItem('userId', user.id.toString());
            localStorage.setItem('nickname', user.nickname);
        } catch (error) {
            console.error('Login failed:', error);
            throw error;
        }
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('userId');
        localStorage.removeItem('nickname');
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isLoading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
