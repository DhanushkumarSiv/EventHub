import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { Calendar, Ticket, User as UserIcon, LogOut, ShieldCheck, Sparkles } from "lucide-react";
import { useAuth } from "../../context/AuthContext";

export const Navbar = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="sticky top-0 z-40 bg-white/95 backdrop-blur-md border-b border-slate-200/80 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2.5 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-orange-600 to-amber-500 flex items-center justify-center text-white shadow-md shadow-orange-500/20 group-hover:scale-105 transition-transform">
              <span className="font-extrabold text-xl">E</span>
            </div>
            <span className="font-bold text-xl tracking-tight text-slate-900">
              Event<span className="text-orange-500">Hub</span>
            </span>
          </Link>

          {/* Center Links */}
          <nav className="hidden md:flex items-center gap-1">
            <Link
              to="/events"
              className="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition flex items-center gap-2"
            >
              <Calendar className="w-4 h-4 text-orange-500" />
              Events
            </Link>

            {isAuthenticated && !isAdmin && (
              <Link
                to="/registrations/my"
                className="px-4 py-2 rounded-xl text-sm font-medium text-slate-600 hover:text-slate-900 hover:bg-slate-50 transition flex items-center gap-2"
              >
                <Ticket className="w-4 h-4 text-orange-500" />
                My Registrations
              </Link>
            )}

            {isAdmin && (
              <Link
                to="/admin/dashboard"
                className="px-4 py-2 rounded-xl text-sm font-medium text-purple-700 bg-purple-50 hover:bg-purple-100 transition flex items-center gap-2"
              >
                <ShieldCheck className="w-4 h-4 text-purple-600" />
                Admin Console
              </Link>
            )}
          </nav>

          {/* Right Action Buttons */}
          <div className="flex items-center gap-3">
            {isAuthenticated ? (
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-2.5 px-3 py-1.5 bg-slate-50 rounded-xl border border-slate-200">
                  <div className="w-7 h-7 rounded-lg bg-orange-100 text-orange-700 font-bold text-xs flex items-center justify-center">
                    {user?.name?.charAt(0).toUpperCase() || "U"}
                  </div>
                  <div className="hidden sm:block text-left">
                    <p className="text-xs font-semibold text-slate-800 leading-tight">{user?.name}</p>
                    <span className="text-[10px] uppercase font-bold tracking-wider text-orange-600">
                      {user?.role}
                    </span>
                  </div>
                </div>

                <button
                  onClick={handleLogout}
                  title="Log out"
                  className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-xl transition cursor-pointer"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Link
                  to="/login"
                  className="px-4 py-2 text-sm font-medium text-slate-700 hover:text-slate-900 hover:bg-slate-100 rounded-xl transition"
                >
                  Log in
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 text-sm font-semibold text-white bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 rounded-xl shadow-xs shadow-orange-500/20 transition flex items-center gap-1.5"
                >
                  <Sparkles className="w-4 h-4" />
                  Sign up
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};
