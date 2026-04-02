import React, { createContext, useContext, useState, useEffect } from "react";
import { authAPI } from "../api/services";

const AuthContext = createContext(null);

// Read from localStorage synchronously so the very first render already knows
// whether the user is logged in — this prevents the flash-to-login on reload.
function readStoredUser() {
  try {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  // ← initialise synchronously from localStorage, NOT in useEffect
  const [user, setUser] = useState(readStoredUser);
  const [authChecked, setAuthChecked] = useState(false);

  useEffect(() => {
    // On mount, verify the session cookie is still valid with the server.
    // If the server says 401/403 the axios interceptor will clear localStorage
    // and redirect to /login automatically.
    const stored = readStoredUser();
    if (!stored) {
      // No stored user at all — nothing to verify
      setAuthChecked(true);
      return;
    }

    // Ping the backend to confirm the session cookie is still alive
    authAPI.me()
      .then((res) => {
        // Session still valid — keep/refresh user data from server
        const fresh = res.data.data;
        localStorage.setItem("user", JSON.stringify(fresh));
        setUser(fresh);
      })
      .catch(() => {
        // Session expired or invalid — clear local state
        localStorage.removeItem("user");
        setUser(null);
      })
      .finally(() => setAuthChecked(true));
  }, []);

  const login = (userData) => {
    localStorage.setItem("user", JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem("user");
    setUser(null);
  };

  const isAdmin = user?.type === "ADMIN";

  // Don't render protected routes until we've verified the session once
  if (!authChecked && readStoredUser()) {
    return null; // or a full-page spinner if you prefer
  }

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin, isLoggedIn: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
