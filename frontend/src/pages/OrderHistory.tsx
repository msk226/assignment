import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ShoppingBag, Calendar, AlertCircle } from 'lucide-react';
import { cn } from '../lib/utils';

interface Order {
    id: number;
    productName: string;
    pointsUsed: number;
    status: 'COMPLETED' | 'CANCELLED';
    createdAt: string;
}

const OrderHistory: React.FC = () => {
    const { data: orders, isLoading, isError } = useQuery<Order[]>({
        queryKey: ['orders'],
        queryFn: async () => {
            const res = await apiClient.get('/api/orders');
            return res.data;
        }
    });

    if (isLoading) {
        return <div className="flex justify-center items-center h-full">Loading...</div>;
    }

    if (isError) {
        return (
            <div className="flex flex-col justify-center items-center h-full gap-4 p-6">
                <AlertCircle size={40} className="text-red-400" />
                <p className="text-gray-600 text-sm text-center">주문 내역을 불러오는 데 실패했습니다.</p>
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
        <div className="bg-gray-50 min-h-full">
            <div className="bg-white p-4 shadow-sm mb-4">
                <h2 className="text-lg font-bold">주문 내역</h2>
            </div>

            <div className="px-4 space-y-3 pb-6">
                {orders?.length === 0 ? (
                    <div className="text-center py-10 text-gray-500 text-sm bg-white rounded-xl shadow-sm">
                        아직 주문 내역이 없습니다.
                    </div>
                ) : (
                    orders?.map((order) => (
                        <div key={order.id} className="bg-white p-4 rounded-2xl shadow-sm flex justify-between items-center">
                            <div className="flex items-center gap-3">
                                <div className={cn(
                                    "w-12 h-12 rounded-lg flex items-center justify-center shrink-0",
                                    order.status === 'CANCELLED' ? "bg-gray-100 text-gray-400" : "bg-primary text-gray-900"
                                )}>
                                    <ShoppingBag size={20} className={order.status === 'CANCELLED' ? "" : "text-gray-900"} />
                                </div>
                                <div>
                                    <div className="font-bold text-gray-900 line-clamp-1">
                                        {order.productName}
                                    </div>
                                    <div className="text-xs text-gray-600 flex items-center mt-1">
                                        <Calendar size={10} className="mr-1" />
                                        {new Date(order.createdAt).toLocaleString()}
                                    </div>
                                    {order.status === 'CANCELLED' && (
                                        <div className="text-[10px] text-red-500 font-bold mt-0.5">주문 취소됨</div>
                                    )}
                                </div>
                            </div>

                            <div className="text-right">
                                <div className={cn(
                                    "font-bold text-sm px-2.5 py-1 rounded-lg inline-block",
                                    order.status === 'CANCELLED' ? "bg-gray-100 text-gray-400 line-through" : "bg-primary text-gray-900"
                                )}>
                                    -{order.pointsUsed.toLocaleString()} P
                                </div>
                                <div className="text-xs text-gray-500 mt-1">
                                    {order.status === 'COMPLETED' ? '구매 완료' : '환불 완료'}
                                </div>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default OrderHistory;
