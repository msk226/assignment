import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiClient } from '../api/client';
import RouletteWheel from '../components/RouletteWheel';
import { Trophy, AlertCircle, CalendarDays } from 'lucide-react';
import { cn } from '../lib/utils';
import { useAuth } from '../contexts/AuthContext';

interface RouletteStatus {
    hasParticipatedToday: boolean;
    todayPoints: number | null;
    remainingBudget: number;
    totalBudget: number;
}

interface SpinResult {
    points: number;
    remainingBudget: number;
    message: string;
}

const Home: React.FC = () => {
    const { } = useAuth();
    const queryClient = useQueryClient();
    const [isSpinning, setIsSpinning] = useState(false);
    const [targetAngle, setTargetAngle] = useState(0);
    const [spinResult, setSpinResult] = useState<SpinResult | null>(null);

    const { data: status, isLoading: isStatusLoading } = useQuery<RouletteStatus>({
        queryKey: ['rouletteStatus'],
        queryFn: async () => {
            const res = await apiClient.get('/api/roulette/status');
            return res.data;
        },
    });

    const spinMutation = useMutation({
        mutationFn: async () => {
            const res = await apiClient.post('/api/roulette/spin');
            return res.data as SpinResult;
        },
        onSuccess: (data) => {
            // Calculate minimal spins (5) + random offset to land on/near the result?
            // Actually, since the wheel is purely visual here (segments don't strictly map to values 1:1 in backend logic which is random 100-1000),
            // we will just spin effectively. 
            // If we want to be realistic, we need to map points to segments.
            // My wheel has 100, 500, 1000, 200, 300, 400.
            // If result is 350, it's not on the wheel.
            // The spec says 100~1000 random. 
            // So I should probably just show a generic "Win" or map to generic segments.
            // Let's assume the wheel is "representative".
            // I will just rotate it by a large random amount + (points % 360)? No.
            // Just spin for effect, then show the result modal.

            const randomSpins = 5 + Math.random() * 5;
            const newAngle = targetAngle + 360 * randomSpins + Math.random() * 360;
            setTargetAngle(newAngle);
            setIsSpinning(true);
            setSpinResult(data);
        },
        onError: (err: any) => {
            alert(err.response?.data?.message || "참여에 실패했습니다.");
        }
    });

    const handleSpin = () => {
        if (status?.hasParticipatedToday) return;
        spinMutation.mutate();
    };

    const handleSpinEnd = () => {
        setIsSpinning(false);
        queryClient.invalidateQueries({ queryKey: ['rouletteStatus'] }); // Refresh status
        // Show Result Dialog/Toast here if needed, or just rely on the UI update
    };

    if (isStatusLoading || !status) {
        return <div className="flex justify-center items-center h-full">Loading...</div>;
    }

    const budgetPercent = (status.remainingBudget / status.totalBudget) * 100;

    return (
        <div className="flex flex-col h-full bg-gray-50">
            {/* Top Banner Area */}
            {/* Top Banner Area */}
            <div className="bg-primary pt-8 pb-16 px-6 rounded-b-[40px] shadow-lg text-gray-900 text-center">
                <h2 className="text-2xl font-bold mb-1">오늘의 행운을 잡으세요!</h2>
                <p className="text-gray-800 text-sm">매일 1회, 최대 1,000 포인트 획득 기회</p>
            </div>

            {/* Main Content Card */}
            <div className="flex-1 px-4 -mt-10 pb-6">
                <div className="bg-white rounded-2xl shadow-xl p-6 flex flex-col items-center min-h-[400px]">

                    {/* Budget Status */}
                    <div className="w-full mb-8">
                        <div className="flex justify-between text-sm mb-2 text-gray-700 font-medium">
                            <span>오늘의 예산</span>
                            <span className={cn(
                                "px-2.5 py-0.5 rounded-full text-xs font-bold",
                                status.remainingBudget === 0 ? "bg-red-100 text-red-500" : "bg-primary text-gray-900"
                            )}>
                                {status.remainingBudget.toLocaleString()} P 남음
                            </span>
                        </div>
                        <div className="w-full bg-gray-900 rounded-full h-2.5 overflow-hidden">
                            <div
                                className={cn("h-2.5 rounded-full transition-all duration-500",
                                    budgetPercent < 20 ? "bg-red-500" : "bg-primary"
                                )}
                                style={{ width: `${Math.max(budgetPercent, 0)}%` }}
                            />
                        </div>
                        {status.remainingBudget === 0 && (
                            <div className="flex items-center text-xs text-red-500 mt-2 bg-red-50 p-2 rounded">
                                <AlertCircle size={14} className="mr-1" />
                                오늘의 예산이 모두 소진되었습니다. 내일 다시 도전해주세요!
                            </div>
                        )}
                    </div>

                    {/* Roulette Section */}
                    <div className="flex-1 flex flex-col justify-center items-center w-full">
                        <div className="mb-8 relative">
                            <RouletteWheel
                                isSpinning={isSpinning}
                                onSpinEnd={handleSpinEnd}
                                targetAngle={targetAngle}
                            />

                            {/* Result Overlay (Only when finished spinning and has result today) */}
                            {!isSpinning && status.hasParticipatedToday && (
                                <div className="absolute inset-0 z-20 flex items-center justify-center bg-black/40 backdrop-blur-[2px] rounded-full">
                                    <div className="bg-white p-4 rounded-xl shadow-2xl text-center transform scale-110 animate-bounce-in">
                                        <Trophy className="mx-auto text-yellow-500 mb-1" size={32} />
                                        <div className="text-xs text-gray-600">오늘의 결과</div>
                                        <div className="text-xl font-bold bg-primary text-gray-900 px-4 py-1.5 rounded-full inline-block mt-1">
                                            {status.todayPoints ?? spinResult?.points ?? 0} P
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Status Message / Action Button */}
                        <div className="text-center w-full">
                            {status.hasParticipatedToday ? (
                                <button
                                    disabled
                                    className="w-full py-4 bg-gray-200 text-gray-500 rounded-xl font-bold text-lg cursor-not-allowed"
                                >
                                    내일 또 오세요!
                                </button>
                            ) : status.remainingBudget <= 0 ? (
                                <button
                                    disabled
                                    className="w-full py-4 bg-gray-200 text-gray-400 rounded-xl font-bold text-lg cursor-not-allowed"
                                >
                                    예산 소진됨
                                </button>
                            ) : (
                                <button
                                    onClick={handleSpin}
                                    disabled={isSpinning || spinMutation.isPending}
                                    className={cn(
                                        "w-full py-4 rounded-full font-bold text-lg text-gray-900 shadow-lg transform transition-all active:scale-95",
                                        "bg-primary hover:bg-[#c2e300]",
                                        (isSpinning || spinMutation.isPending) && "opacity-80 cursor-wait"
                                    )}
                                >
                                    {isSpinning ? "행운을 비는 중..." : "룰렛 돌리기!"}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* Footer Info */}
            <div className="px-6 pb-6 text-center text-xs text-gray-400">
                <p className="flex justify-center items-center gap-1">
                    <CalendarDays size={12} /> 포인트 유효기간은 30일입니다.
                </p>
            </div>
        </div>
    );
};

export default Home;
