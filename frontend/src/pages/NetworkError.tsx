import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { WifiOff, RefreshCw } from 'lucide-react';

const NetworkError: React.FC = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const from = searchParams.get('from') || '/home';

    const handleRetry = () => {
        // Navigate back to the previous page or the specific 'from' route
        // Using replace to avoid stacking error pages in history
        navigate(from, { replace: true });
    };

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-6 text-center">
            <div className="bg-white p-8 rounded-2xl shadow-lg max-w-sm w-full flex flex-col items-center animate-fade-in-up">
                <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mb-6 text-red-500">
                    <WifiOff size={32} />
                </div>

                <h1 className="text-xl font-bold text-gray-900 mb-2">
                    네트워크 연결 상태를 확인해주세요
                </h1>

                <p className="text-gray-500 text-sm mb-8 leading-relaxed">
                    서버와의 연결이 지연되거나 끊어졌습니다.<br />
                    잠시 후 다시 시도해 주세요.
                </p>

                <button
                    onClick={handleRetry}
                    className="w-full bg-black text-white py-4 rounded-xl font-medium flex items-center justify-center gap-2 active:scale-95 transition-transform hover:bg-gray-800"
                >
                    <RefreshCw size={20} />
                    다시 시도
                </button>
            </div>
        </div>
    );
};

export default NetworkError;
