import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  CalendarDays,
  PlusCircle,
  UserCheck,
  ExternalLink,
  LogOut,
  Sparkles,
} from "lucide-react";
import { useAuth } from "../../context/AuthContext";

export const AdminSidebar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const navItems = [
    { name: "Dashboard", path: "/admin/dashboard", icon: LayoutDashboard },
    { name: "Manage Events", path: "/admin/events", icon: CalendarDays },
    { name: "Create Event", path: "/admin/events/new", icon: PlusCircle },
    { name: "Attendance & Check-in", path: "/admin/attendance", icon: UserCheck },
  ];

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between shrink-0 min-h-screen text-slate-300">
      <div>
        {/* Brand / Logo */}
        <div className="h-16 px-6 flex items-center gap-3 border-b border-slate-800">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-orange-500 to-amber-400 flex items-center justify-center text-white font-extrabold shadow-md shadow-orange-500/20">
            E
          </div>
          <div>
            <span className="font-bold text-lg text-white tracking-tight">EventHub</span>
            <span className="text-[10px] block font-bold tracking-widest text-orange-400 uppercase">
              Admin Console
            </span>
          </div>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-1">
          <p className="px-3 py-2 text-[11px] font-semibold tracking-wider uppercase text-slate-500">
            Navigation
          </p>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition ${
                    isActive
                      ? "bg-orange-500 text-white shadow-xs shadow-orange-500/30"
                      : "text-slate-400 hover:text-white hover:bg-slate-800/60"
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                {item.name}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Footer Profile & Portal Link */}
      <div className="p-4 border-t border-slate-800 space-y-3">
        <NavLink
          to="/events"
          className="flex items-center justify-between px-3 py-2 text-xs font-medium text-slate-400 hover:text-white hover:bg-slate-800/60 rounded-xl transition"
        >
          <span className="flex items-center gap-2">
            <Sparkles className="w-3.5 h-3.5 text-orange-400" />
            Switch to Public Portal
          </span>
          <ExternalLink className="w-3.5 h-3.5 text-slate-500" />
        </NavLink>

        <div className="flex items-center justify-between p-3 bg-slate-800/60 rounded-xl border border-slate-800">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-8 h-8 rounded-lg bg-orange-500/20 text-orange-400 font-bold text-xs flex items-center justify-center shrink-0">
              {user?.name?.charAt(0).toUpperCase() || "A"}
            </div>
            <div className="truncate">
              <p className="text-xs font-semibold text-white truncate">{user?.name}</p>
              <p className="text-[10px] text-slate-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            title="Log out"
            className="text-slate-400 hover:text-rose-400 p-1.5 hover:bg-rose-500/10 rounded-lg transition shrink-0 cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
};
