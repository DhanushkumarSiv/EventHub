import React, { createContext, useContext, useState, useEffect } from "react";
import { authService } from "../services/authService";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const savedUser = localStorage.getItem("eventhub_user");
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  const [token, setToken] = useState(() => localStorage.getItem("eventhub_token") || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      const savedToken = localStorage.getItem("eventhub_token");
      if (savedToken) {
        try {
          const currentUser = await authService.getCurrentUser();
          setUser(currentUser);
          localStorage.setItem("eventhub_user", JSON.stringify(currentUser));
        } catch (err) {
          console.error("Token verification failed:", err);
          logout();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email, password) => {
    const data = await authService.login(email, password);
    setToken(data.token);
    setUser(data.user);
    localStorage.setItem("eventhub_token", data.token);
    localStorage.setItem("eventhub_user", JSON.stringify(data.user));
    return data.user;
  };

  const register = async (name, email, password) => {
    const data = await authService.register(name, email, password);
    setToken(data.token);
    setUser(data.user);
    localStorage.setItem("eventhub_token", data.token);
    localStorage.setItem("eventhub_user", JSON.stringify(data.user));
    return data.user;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem("eventhub_token");
    localStorage.removeItem("eventhub_user");
  };

  const isAuthenticated = !!token && !!user;
  const isAdmin = user?.role === "ADMIN";
  const isStudent = user?.role === "STUDENT";

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        isAuthenticated,
        isAdmin,
        isStudent,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
