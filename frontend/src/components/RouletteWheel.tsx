import React from 'react';
import { motion, useAnimation } from 'framer-motion';

interface RouletteWheelProps {
    isSpinning: boolean;
    onSpinEnd: () => void;
    targetAngle?: number; // Target rotation angle in degrees
}

const RouletteWheel: React.FC<RouletteWheelProps> = ({ isSpinning, onSpinEnd, targetAngle }) => {
    const controls = useAnimation();

    React.useEffect(() => {
        if (isSpinning && targetAngle !== undefined) {
            controls.start({
                rotate: targetAngle,
                transition: {
                    duration: 4,
                    ease: [0.2, 0.8, 0.2, 1], // Cubic bezier for "ease-out" effect like a real wheel
                },
            }).then(() => {
                onSpinEnd();
            });
        }
    }, [isSpinning, targetAngle, controls, onSpinEnd]);

    // Gradient slices for visual appeal
    const slices = [
        { color: '#3b82f6', label: '100' },
        { color: '#8b5cf6', label: '500' },
        { color: '#ec4899', label: '1000' },
        { color: '#10b981', label: '200' },
        { color: '#f59e0b', label: '300' },
        { color: '#6366f1', label: '400' },
    ];

    const sliceAngle = 360 / slices.length;

    return (
        <div className="relative w-64 h-64 mx-auto">
            {/* Pointer */}
            <div className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-4 z-10">
                <div className="w-0 h-0 border-l-[10px] border-l-transparent border-r-[10px] border-r-transparent border-t-[20px] border-t-red-500 drop-shadow-lg" />
            </div>

            {/* Wheel */}
            <motion.div
                className="w-full h-full rounded-full border-4 border-white shadow-2xl overflow-hidden relative"
                animate={controls}
                initial={{ rotate: 0 }}
            >
                {slices.map((_, index) => (
                    <div
                        key={index}
                        className="absolute w-full h-full text-center"
                        style={{
                            transform: `rotate(${index * sliceAngle}deg)`,
                            clipPath: `polygon(50% 50%, 50% 0, ${50 + 50 * Math.tan(Math.PI / slices.length)}% 0)`,
                            // This clipPath is approximate for simplicity. For a perfect circle segments, conic-gradient is better but hard to label.
                            // Let's stick to conic gradient for background and rotate labels?
                            // Actually, simplified approach: Conic gradient background
                        }}
                    >
                        {/* Visual Slice is tricky with just divs. Let's use Conic Gradient on the parent and overlay labels. */}
                    </div>
                ))}

                {/* Better visual implementation using Conic Gradient */}
                <div
                    className="absolute inset-0 w-full h-full rounded-full"
                    style={{
                        background: `conic-gradient(
                    ${slices.map((s, i) => `${s.color} ${i * (100 / slices.length)}% ${(i + 1) * (100 / slices.length)}%`).join(', ')}
                )`
                    }}
                />

                {/* Labels */}
                {slices.map((slice, index) => (
                    <div
                        key={index}
                        className="absolute w-full h-full flex justify-center pt-4"
                        style={{
                            transform: `rotate(${index * sliceAngle + sliceAngle / 2}deg)`,
                        }}
                    >
                        <span className="text-white font-bold text-lg drop-shadow-md">{slice.label}</span>
                    </div>
                ))}

                {/* Center decorative circle */}
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-12 h-12 bg-white rounded-full shadow-inner flex items-center justify-center">
                    <div className="w-8 h-8 bg-gray-200 rounded-full animate-pulse" />
                </div>
            </motion.div>

            {/* Shadow/Stand */}
            <div className="absolute -bottom-8 left-1/2 -translate-x-1/2 w-32 h-4 bg-black/20 blur-xl rounded-full" />
        </div>
    );
};

export default RouletteWheel;
