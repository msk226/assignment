import React from 'react';
import { NavLink, Outlet, useNavigate, Link } from 'react-router-dom';
import { Home, Coins, ShoppingBag, ClipboardList, LogOut } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { cn } from '../lib/utils';

import logo from '../assets/logo.png';

const MobileLayout: React.FC = () => {
    const { logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <div className="flex flex-col h-screen bg-gray-50 w-full max-w-lg mx-auto shadow-xl relative overflow-hidden">
            <header className="bg-white border-b border-gray-100 px-4 py-3 flex justify-between items-center shrink-0 z-10">
                <Link to="/home" className="flex items-center gap-2">
                    <img src={logo} alt="Logo" className="w-8 h-8 rounded-full border border-gray-100" />
                    <h1 className="text-lg font-bold">
                        <span className="bg-primary text-gray-900 px-3 py-1 rounded-full">Point Roulette</span>
                    </h1>
                </Link>
                <button
                    onClick={handleLogout}
                    className="p-2 text-gray-500 hover:text-gray-900 rounded-full hover:bg-gray-100 transition-colors"
                    aria-label="Logout"
                >
                    <LogOut size={20} />
                </button>
            </header>

            <main className="flex-1 overflow-y-auto pb-4">
                <Outlet />
            </main>

            <nav className="bg-white border-t border-gray-200 w-full shrink-0 pb-safe z-20">
                <div className="flex justify-around items-center h-16">
                    <NavItem to="/home" icon={Home} label="홈" />
                    <NavItem to="/points" icon={Coins} label="포인트" />
                    <NavItem to="/products" icon={ShoppingBag} label="상품" />
                    <NavItem to="/orders" icon={ClipboardList} label="주문" />
                </div>
            </nav>
        </div>
    );
};

interface NavItemProps {
    to: string;
    icon: React.ElementType;
    label: string;
}

const NavItem: React.FC<NavItemProps> = ({ to, icon: Icon, label }) => {
    return (
        <NavLink
            to={to}
            className={({ isActive }) =>
                cn(
                    "flex flex-col items-center justify-center w-full h-full space-y-1 transition-colors",
                    isActive
                        ? "text-gray-900 font-bold"
                        : "text-gray-500 hover:text-gray-700"
                )
            }
        >
            <Icon size={24} strokeWidth={2.5} className="mb-0.5" />
            <span className="text-xs font-medium">{label}</span>
        </NavLink>
    );
};

export default MobileLayout;
