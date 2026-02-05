import { apiClient } from './client';

export interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    stock: number;
    imageUrl?: string;
    isActive: boolean;
    createdAt: string;
}

export interface Order {
    id: number;
    userId: number;
    productId: number;
    productName: string;
    pointsUsed: number;
    status: 'COMPLETED' | 'CANCELLED';
    createdAt: string;
}

export interface Budget {
    date: string;
    totalBudget: number;
    usedBudget: number;
    remainingBudget: number;
}

export interface DashboardStats {
    date: string;
    totalBudget: number;
    usedBudget: number;
    remainingBudget: number;
    participantCount: number;
    totalPointsDistributed: number;
}

export interface RouletteParticipation {
    id: number;
    userId: number;
    nickname: string;
    points: number;
    status: 'PARTICIPATED' | 'CANCELLED';
    createdAt: string;
    cancelledAt: string | null;
}

// 예산 관련 API
export const budgetApi = {
    getBudget: (date?: string) => apiClient.get<Budget>(`/api/admin/budget`, { params: { date } }).then(res => res.data),
    updateBudget: (data: { date: string, totalBudget: number }) => apiClient.put(`/api/admin/budget`, { totalBudget: data.totalBudget }, { params: { date: data.date } }).then(res => res.data),
};

// 상품 관련 API
export const productApi = {
    getProducts: () => apiClient.get<Product[]>('/api/admin/products').then(res => res.data),
    createProduct: (data: Pick<Product, 'name' | 'description' | 'price' | 'stock' | 'imageUrl'>) => apiClient.post('/api/admin/products', data).then(res => res.data),
    updateProduct: (id: number, data: Partial<Pick<Product, 'name' | 'description' | 'price' | 'stock' | 'imageUrl'>>) => apiClient.put(`/api/admin/products/${id}`, data).then(res => res.data),
    deleteProduct: (id: number) => apiClient.delete(`/api/admin/products/${id}`).then(res => res.data),
};

// 주문 관련 API
export const orderApi = {
    getOrders: () => apiClient.get<Order[]>('/api/admin/orders').then(res => res.data),
    cancelOrder: (id: number) => apiClient.delete(`/api/admin/orders/${id}`).then(res => res.data),
};

// 룰렛 관련 API
export const rouletteApi = {
    getRouletteList: () => apiClient.get<RouletteParticipation[]>('/api/admin/roulette').then(res => res.data),
    cancelRoulette: (id: number) => apiClient.post(`/api/admin/roulette/${id}/cancel`).then(res => res.data),
};

// 대시보드 관련 API
export const dashboardApi = {
    getStats: () => apiClient.get<DashboardStats>('/api/admin/dashboard').then(res => res.data),
};
