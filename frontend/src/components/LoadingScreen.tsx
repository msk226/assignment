import React from 'react';
import { Loader2 } from 'lucide-react';
import logo from '../assets/logo.png';

const LoadingScreen: React.FC = () => {
    return (
        <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
            <div className="flex flex-col items-center gap-6 animate-fade-in">
                {/* Logo with pulse effect */}
                <div className="relative">
                    <div className="absolute inset-0 bg-primary/20 blur-xl rounded-full animate-pulse" />
                    <img
                        src={logo}
                        alt="Point Roulette"
                        className="w-20 h-20 rounded-2xl shadow-lg relative z-10"
                    />
                </div>

                {/* Spinner and Text */}
                <div className="flex flex-col items-center gap-3">
                    <Loader2 className="w-8 h-8 text-primary animate-spin" strokeWidth={3} />
                    <p className="text-gray-500 font-medium text-sm tracking-wide animate-pulse">
                        로딩중...
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LoadingScreen;
