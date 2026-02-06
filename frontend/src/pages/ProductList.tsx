import React from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import { ShoppingBag, Coins } from 'lucide-react';
import { cn } from '../lib/utils';

interface Product {
    id: number;
    name: string;
    description?: string;
    price: number;
    stock: number;
    imageUrl?: string;
    isActive: boolean;
}

interface PointBalance {
    totalBalance: number;
}

const ProductList: React.FC = () => {
    const queryClient = useQueryClient();

    const { data: products, isLoading: isProductsLoading } = useQuery<Product[]>({
        queryKey: ['products'],
        queryFn: async () => {
            const res = await apiClient.get('/api/products');
            return res.data;
        }
    });

    const { data: balance, isLoading: isBalanceLoading } = useQuery<PointBalance>({
        queryKey: ['pointBalance'],
        queryFn: async () => {
            const res = await apiClient.get('/api/points/balance');
            return res.data;
        }
    });

    const orderMutation = useMutation({
        mutationFn: async (productId: number) => {
            const res = await apiClient.post('/api/orders', { productId });
            return res.data;
        },
        onSuccess: () => {
            alert('주문이 완료되었습니다!');
            queryClient.invalidateQueries({ queryKey: ['pointBalance'] });
            queryClient.invalidateQueries({ queryKey: ['points'] });
            queryClient.invalidateQueries({ queryKey: ['orders'] });
            queryClient.invalidateQueries({ queryKey: ['products'] }); // Resfresh stock
        },
        onError: (err: any) => {
            alert(err.response?.data?.message || '주문에 실패했습니다.');
        }
    });

    const handleBuy = (productId: number) => {
        if (!window.confirm('정말 구매하시겠습니까?')) return;
        orderMutation.mutate(productId);
    };

    if (isProductsLoading || isBalanceLoading) {
        return <div className="flex justify-center items-center h-full">Loading...</div>;
    }

    const currentBalance = balance?.totalBalance || 0;

    return (
        <div className="bg-gray-50 min-h-full pb-6">
            <div className="bg-white p-4 shadow-sm sticky top-0 z-10 flex justify-between items-center">
                <h2 className="text-lg font-bold">상품 목록</h2>
                <div className="flex items-center text-gray-900 font-bold text-sm bg-primary px-3 py-1 rounded-full shadow-md">
                    <Coins size={14} className="mr-1.5" />
                    {currentBalance.toLocaleString()} P
                </div>
            </div>

            <div className="p-4 grid gap-4 grid-cols-1 sm:grid-cols-2">
                {products?.map((product) => (
                    <div key={product.id} className="bg-white rounded-2xl shadow-sm overflow-hidden flex flex-col">
                        <div className="h-32 bg-gray-200 relative">
                            {product.imageUrl ? (
                                <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
                            ) : (
                                <div className="w-full h-full flex items-center justify-center text-gray-400">
                                    <ShoppingBag size={32} />
                                </div>
                            )}
                            {product.stock <= 0 && (
                                <div className="absolute inset-0 bg-black/50 flex items-center justify-center text-white font-bold">
                                    품절
                                </div>
                            )}
                        </div>

                        <div className="p-4 flex-1 flex flex-col">
                            <h3 className="font-bold text-gray-900 mb-1">{product.name}</h3>
                            <p className="text-xs text-gray-600 line-clamp-2 mb-3 flex-1">
                                {product.description || "설명 없음"}
                            </p>

                            <div className="flex justify-between items-center mb-3">
                                <span className="font-bold text-sm bg-primary text-gray-900 px-2.5 py-1 rounded-lg">
                                    {product.price.toLocaleString()} P
                                </span>
                                <span className="text-xs text-gray-400 font-medium bg-gray-50 px-2 py-1 rounded-md">재고 {product.stock}개</span>
                            </div>

                            <button
                                onClick={() => handleBuy(product.id)}
                                disabled={product.stock <= 0 || currentBalance < product.price || orderMutation.isPending}
                                className={cn(
                                    "w-full py-2.5 rounded-full text-sm font-bold flex items-center justify-center transition-colors",
                                    currentBalance >= product.price && product.stock > 0
                                        ? "bg-primary text-gray-900 hover:bg-primary/90 shadow-sm"
                                        : "bg-gray-100 text-gray-400 cursor-not-allowed"
                                )}
                            >
                                {product.stock <= 0
                                    ? "품절"
                                    : currentBalance < product.price
                                        ? "포인트 부족"
                                        : orderMutation.isPending
                                            ? "처리 중..."
                                            : "구매하기"
                                }
                            </button>
                        </div>
                    </div>
                ))}
            </div>
            {products?.length === 0 && (
                <div className="text-center py-20 text-gray-500">
                    판매 중인 상품이 없습니다.
                </div>
            )}
        </div>
    );
};

export default ProductList;
