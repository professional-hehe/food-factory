import axios from "axios";

const api = axios.create({
  baseURL: "/api",              // proxied to http://localhost:8080/api by CRA dev server
  headers: { "Content-Type": "application/json" },
  withCredentials: true,        // sends JSESSIONID cookie automatically
});

// Handle 401 globally — redirect to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || "";
    // Don't redirect on /auth/me failures — AuthContext handles those gracefully
    const isMeCheck = url.includes("/auth/me");
    if (!isMeCheck && (err.response?.status === 401 || err.response?.status === 403)) {
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default api;
