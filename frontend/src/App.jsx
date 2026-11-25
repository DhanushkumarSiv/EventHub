import React from "react";
import { BrowserRouter, useLocation } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { Navbar } from "./components/layout/Navbar";
import { AppRoutes } from "./routes/AppRoutes";

const AppContent = () => {
  const location = useLocation();
  const isAdminRoute = location.pathname.startsWith("/admin");

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-800 antialiased selection:bg-orange-500 selection:text-white">
      {/* Show Navbar on public and student routes; Admin uses AdminSidebar */}
      {!isAdminRoute && <Navbar />}
      <div className="flex-1">
        <AppRoutes />
      </div>
    </div>
  );
};

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}