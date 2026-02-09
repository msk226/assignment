import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { Clock, CheckCircle2, AlertTriangle } from 'lucide-react';
import { cn } from '../lib/utils';
import { useAuth } from '../contexts/AuthContext';

interface PointItem {
    id: number;
    amount: number;
    usedAmount: number;
    availableAmount: number;
    earnedAt: string;
    expiresAt: string;
    isExpired: boolean;
    daysUntilExpiry: number;
    status: 'EARNED' | 'EXPIRED' | 'CANCELED';
}

interface PointBalance {
    totalBalance: number;
    expiringWithin7Days: number;
}

const MyPoints: React.FC = () => {
    const { } = useAuth();
    const [filterStatus, setFilterStatus] = useState("");

    // Fetch user's point list
    const { data: points, isLoading: isPointsLoading, isError: isPointsError } = useQuery<PointItem[]>({
        queryKey: ['points', filterStatus],
        queryFn: async () => {
            const params = filterStatus ? { status: filterStatus } : {};
            const res = await apiClient.get('/api/points', { params });
            return res.data;
        }
    });

    // Fetch balance summary
    const { data: balance, isLoading: isBalanceLoading, isError: isBalanceError } = useQuery<PointBalance>({
        queryKey: ['pointBalance'],
        queryFn: async () => {
            const res = await apiClient.get('/api/points/balance');
            return res.data;
        }
    });

    if (isPointsLoading || isBalanceLoading) {
        return <div className="flex justify-center items-center h-full">Loading...</div>;
    }

    if (isPointsError || isBalanceError) {
        return (
            <div className="flex flex-col justify-center items-center h-full gap-4 p-6">
                <AlertTriangle size={40} className="text-red-400" />
                <p className="text-gray-600 text-sm text-center">포인트 정보를 불러오는 데 실패했습니다.</p>
                <button
                    onClick={() => window.location.reload()}
                    className="px-6 py-2 bg-primary text-gray-900 rounded-full font-bold text-sm"
                >
                    다시 시도
                </button>
            </div>
        );
    }

    return (
        <div className="bg-gray-50 min-h-full pb-6">
            {/* Header Section */}
            <div className="bg-white p-6 shadow-sm mb-4">
                <h2 className="text-gray-600 text-sm mb-1">내 사용 가능 포인트</h2>
                <div className="text-3xl font-bold text-gray-900">
                    {balance?.totalBalance.toLocaleString()} P
                </div>

                {/* Expiring Points Alert */}
                {balance && balance.expiringWithin7Days > 0 && (
                    <div className="mt-4 flex items-start gap-3 bg-orange-50 p-3 rounded-lg border border-orange-100">
                        <AlertTriangle className="text-orange-500 shrink-0 mt-0.5" size={18} />
                        <div>
                            <p className="text-sm font-bold text-orange-700">소멸 예정 포인트</p>
                            <p className="text-xs text-orange-600 mt-0.5">
                                7일 내에 <span className="font-bold">{balance.expiringWithin7Days.toLocaleString()} P</span>가 소멸됩니다.
                            </p>
                        </div>
                    </div>
                )}
            </div>

            {/* Points History List */}
            <div className="px-4">
                <div className="flex justify-between items-center mb-3 px-1">
                    <h3 className="text-lg font-bold text-gray-800">포인트 내역</h3>
                    <select
                        value={filterStatus}
                        onChange={(e) => setFilterStatus(e.target.value)}
                        className="bg-white border border-gray-200 text-gray-700 text-sm rounded-lg focus:ring-primary focus:border-primary block p-2"
                    >
                        <option value="">전체</option>
                        <option value="EARNED">적립됨</option>
                        <option value="EXPIRED">만료됨</option>
                        <option value="CANCELED">취소됨</option>
                    </select>
                </div>

                <div className="space-y-3">
                    {points?.length === 0 ? (
                        <div className="text-center py-10 text-gray-500 text-sm bg-white rounded-xl shadow-sm">
                            내역이 없습니다.
                        </div>
                    ) : (
                        points?.map((point) => (
                            <div
                                key={point.id}
                                className={cn(
                                    "bg-white p-4 rounded-2xl shadow-sm flex justify-between items-center relative overflow-hidden",
                                    point.isExpired || point.status === 'CANCELED' || point.status === 'EXPIRED' ? "opacity-60 grayscale" : ""
                                )}
                            >
                                <div className="flex items-center gap-3">
                                    <div className={cn(
                                        "w-10 h-10 rounded-full flex items-center justify-center shrink-0",
                                        point.status === 'EARNED' ? "bg-primary text-gray-900" : "bg-gray-100 text-gray-400"
                                    )}>
                                        <CheckCircle2 size={20} />
                                    </div>
                                    <div>
                                        <div className="font-bold text-sm bg-primary text-gray-900 px-3 py-1 rounded-full inline-block mb-1">
                                            +{point.amount.toLocaleString()} P
                                        </div>
                                        <div className="text-xs text-gray-500 font-medium">
                                            {point.status === 'EARNED' ? '획득' : point.status === 'EXPIRED' ? '만료' : '취소'}
                                        </div>
                                        <div className="text-xs text-gray-600 flex items-center mt-1">
                                            <Clock size={10} className="mr-1" />
                                            {new Date(point.earnedAt).toLocaleDateString()}
                                            <span className="mx-1">~</span>
                                            {new Date(point.expiresAt).toLocaleDateString()}
                                        </div>
                                    </div>
                                </div>

                                <div className="text-right">
                                    {point.status === 'EXPIRED' ? (
                                        <span className="text-xs font-bold text-red-400 bg-red-50 px-2 py-1 rounded">만료됨</span>
                                    ) : point.status === 'CANCELED' ? (
                                        <span className="text-xs font-bold text-gray-500 bg-gray-100 px-2 py-1 rounded">취소됨</span>
                                    ) : (
                                        <div className="flex flex-col items-end">
                                            <span className="text-sm font-medium text-gray-700">
                                                잔액: {point.availableAmount.toLocaleString()}
                                            </span>
                                            {point.daysUntilExpiry <= 7 && (
                                                <span className="text-[10px] text-orange-500 font-bold mt-1">
                                                    D-{point.daysUntilExpiry}
                                                </span>
                                            )}
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default MyPoints;
