import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor to add JWT token from localStorage
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("eventhub_token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle common errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Optional: clear auth on unauthorized if token is expired
      const isAuthEndpoint = error.config?.url?.includes("/auth/");
      if (!isAuthEndpoint) {
        localStorage.removeItem("eventhub_token");
        localStorage.removeItem("eventhub_user");
        if (window.location.pathname !== "/login") {
          window.location.href = "/login";
        }
      }
    }
    return Promise.reject(error);
  }
);
