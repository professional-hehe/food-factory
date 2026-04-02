import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { authAPI } from "../api/services";
import toast from "react-hot-toast";

export default function Navbar() {
  const { user, logout, isLoggedIn, isAdmin } = useAuth();
  const { itemCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try { await authAPI.logout(); } catch {}
    logout();
    toast.success("Logged out!");
    navigate("/login");
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        {/* Logo */}
        <Link to="/" className="navbar-logo">🍽️ FoodApp</Link>

        {/* Nav links */}
        <nav className="navbar-links">
          <Link to="/" className="nav-link">Restaurants</Link>

          {isLoggedIn && (
            <>
              <Link to="/cart" className="nav-link nav-cart-link">
                🛒 Cart
                {itemCount > 0 && (
                  <span className="cart-badge">{itemCount > 9 ? "9+" : itemCount}</span>
                )}
              </Link>
              <Link to="/orders" className="nav-link">Orders</Link>
              {isAdmin && (
                <Link to="/admin" className="nav-link">⚙️ Admin</Link>
              )}
            </>
          )}

          <div className="nav-divider" />

          {isLoggedIn ? (
            <>
              <span className="navbar-user-name">Hi, {user?.name?.split(" ")[0]} 👋</span>
              <button onClick={handleLogout} className="btn btn-white btn-sm">Sign out</button>
            </>
          ) : (
            <>
              <Link to="/login" className="nav-link">Sign in</Link>
              <Link to="/register" className="btn btn-black btn-sm">Sign up</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
