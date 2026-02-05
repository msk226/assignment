import React, { useEffect, useState, useRef } from 'react';
import { motion, useAnimation } from 'framer-motion';

// --- Types ---
interface SlotMachineProps {
    isSpinning: boolean;
    onSpinEnd: () => void;
    winPoints?: number | null;
}

// --- Constants ---
const SYMBOLS = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];
const SYMBOL_HEIGHT = 120; // Must match CSS height

// A Reel component that handles its own spinning and stopping logic
// "Senior" Touch: Use seamless loop logic without visual jumps.
const Reel = ({
    isSpinning,
    targetDigit,
    stopDelay,
    onReelStop
}: {
    isSpinning: boolean;
    targetDigit: number;
    stopDelay: number;
    onReelStop: () => void;
}) => {
    const controls = useAnimation();

    // We render a long strip of numbers: [0..9, 0..9, ... ] 
    // We spin downwards (y decreases).

    // Improved Effect Logic
    const isFirstRun = useRef(true);

    useEffect(() => {
        if (isSpinning) {
            controls.start({
                y: [0, -SYMBOL_HEIGHT * 10],
                transition: { duration: 0.35, ease: "linear", repeat: Infinity }
            });
            isFirstRun.current = false;
        } else {
            if (isFirstRun.current) {
                // Initial render: just set position
                controls.set({ y: -(targetDigit * SYMBOL_HEIGHT) });
                return;
            }

            // Stop Sequence
            const stopSequence = async () => {
                // Wait for delay
                await new Promise(r => setTimeout(r, stopDelay * 1000));

                // Seamless stop hack:
                // We are currently in an infinite loop 0 -> -1200.
                // We stop it.
                // We set Y to 0 (effectively warping to start of a new loop - invisible if fast enough)
                // Then animate to -(target * 120) + maybe extra loops.

                // To make it truly smooth without "warp jump":
                // We animate to a value that is logically "after" the current spin.
                // But simply resetting to 0 and doing a long Ease-Out is visually very close to perfect for fast reels.
                controls.stop();

                // "Landing" animation
                // We scroll '3 full sets' + 'target index' to ensure enough distance for ease-out.
                const landingY = -((3 * 10 * SYMBOL_HEIGHT) + (targetDigit * SYMBOL_HEIGHT));

                // Start from 0 (visual reset) to landingY
                controls.set({ y: 0 });

                await controls.start({
                    y: landingY,
                    transition: {
                        duration: 2.5,  // Slow down over 1.5s
                        ease: "easeOut"
                    }
                });

                onReelStop();
            };
            stopSequence();
        }
    }, [isSpinning, targetDigit, stopDelay, controls]); // Dependencies

    // We render 5 sets of 0-9 to allow for the "3 full sets" landing + buffer
    const renderSymbols = () => {
        const sets = [];
        for (let i = 0; i < 6; i++) { // 6 sets to cover start(1) + landing(3) + buffer
            sets.push(...SYMBOLS);
        }
        return sets;
    };

    return (
        <div className="overflow-hidden h-[120px] w-20 bg-white border-2 border-gray-200 rounded-lg mx-1 relative shadow-inner">
            <motion.div
                animate={controls}
                className="flex flex-col items-center"
            >
                {renderSymbols().map((symbol, i) => (
                    <div key={i} className="h-[120px] w-full flex items-center justify-center text-5xl font-bold font-mono text-gray-900 shrink-0">
                        {symbol}
                    </div>
                ))}
            </motion.div>
            {/* Gradient Overlay for 3D effect */}
            <div className="absolute inset-0 bg-gradient-to-b from-black/20 via-transparent to-black/20 pointer-events-none" />
        </div>
    );
};

const SlotMachine: React.FC<SlotMachineProps> = ({ isSpinning, onSpinEnd, winPoints }) => {
    // Digits state: [Thousands, Hundreds, Tens, Ones]
    const [digits, setDigits] = useState([0, 0, 0, 0]);
    const [internalIsSpinning, setInternalIsSpinning] = useState(false);

    // We treat the "Spin Phase" as the period where reels are moving.
    // When parent says isSpinning=true, we start.
    // We assume the parent WILL NOT set isSpinning=false until we call onSpinEnd.
    // Actually, usually parent toggles isSpinning=true (start), then waits for onSpinEnd to toggle false.
    // OR parent sets isSpinning=true, gets API result, then KEEPS isSpinning=true? 
    // The previous code had a bug where parent didn't toggle off.
    // The 'Internal' state helps us manage the "Spin -> Wait -> Stop" transition regardless of parent prop persistence.
    // BUT user requirements say: "Animation ends -> exactly at target".

    // Logic:
    // 1. Parent sets isSpinning=true.
    // 2. We set internalIsSpinning=true (Reels start infinite spin).
    // 3. We wait for API result (winPoints).
    // 4. Once we have winPoints, we wait a minimum spin time (e.g. 2s).
    // 5. Then we set internalIsSpinning=false (Reels trigger stop sequence).
    // 6. Each reel stops. We need to know when ALL stopped.
    // 7. Call onSpinEnd().

    const stoppedReelsCount = useRef(0);

    useEffect(() => {
        if (isSpinning) {
            stoppedReelsCount.current = 0;
            setInternalIsSpinning(true);

            // Minimum spin time before allowing stop
            const minSpinTimer = setTimeout(() => {
                // Only stop if we actually have a result
                if (winPoints !== undefined && winPoints !== null) {
                    setInternalIsSpinning(false);
                }
            }, 500); // Start stopping sequence almost immediately after API result if wanted, or fixed delay.
            // User said: "All fast spin start" -> "API target received" -> "Stop sequential".
            // Let's force a minimum visual engagement of 1s.

            return () => clearTimeout(minSpinTimer);
        }
    }, [isSpinning]); // Only trigger on start

    // Watch for winPoints update while spinning
    useEffect(() => {
        if (isSpinning && winPoints !== null && winPoints !== undefined) {
            const formatted = winPoints.toString().padStart(4, '0');
            const newDigits = formatted.split('').map(d => parseInt(d));
            setDigits(newDigits);

            // Ensure we are spinning for at least X seconds? 
            // We can rely on a timeout in the start effect, or just trigger stop here with a delay?
            // Let's use a robust approach:
            // If we are spinning, and get points, schedule stop.
            const stopTimer = setTimeout(() => {
                setInternalIsSpinning(false);
            }, 1500); // 1.5s pure spin
            return () => clearTimeout(stopTimer);
        }
    }, [isSpinning, winPoints]);


    const handleReelStop = () => {
        stoppedReelsCount.current += 1;
        if (stoppedReelsCount.current === 4) {
            // All reels stopped
            setTimeout(onSpinEnd, 500); // Small buffer before enabling button
        }
    };

    return (
        <div className="flex justify-center items-center bg-gray-900 p-6 rounded-3xl shadow-xl border-4 border-primary relative">
            <div className="flex space-x-2 bg-gray-800 p-4 rounded-xl relative z-10">
                {/* 
                  Reels Order: Thousands [0], Hundreds [1], Tens [2], Ones [3] 
                  Stop Order: Ones -> Tens -> Hundreds -> Thousands
                  Delays: Ones(0s), Tens(0.5s), Hundreds(1.0s), Thousands(1.5s)
                */}
                <Reel isSpinning={internalIsSpinning} targetDigit={digits[0]} stopDelay={1.5} onReelStop={handleReelStop} />
                <Reel isSpinning={internalIsSpinning} targetDigit={digits[1]} stopDelay={1.0} onReelStop={handleReelStop} />
                <Reel isSpinning={internalIsSpinning} targetDigit={digits[2]} stopDelay={0.5} onReelStop={handleReelStop} />
                <Reel isSpinning={internalIsSpinning} targetDigit={digits[3]} stopDelay={0.0} onReelStop={handleReelStop} />
            </div>

            <div className="absolute -top-3 bg-primary text-gray-900 font-bold px-4 py-1 rounded-full text-xs shadow-md border-2 border-gray-900 z-20">
                POINT JACKPOT
            </div>
        </div>
    );
};

export default SlotMachine;
