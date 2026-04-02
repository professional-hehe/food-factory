import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { restaurantAPI, menuAPI } from "../api/services";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import toast from "react-hot-toast";

const CATEGORY_COLORS = {
  VEG: "#C8F7D5",
  NON_VEG: "#FFCDD2",
  EGG: "#FFD166",
};

export default function RestaurantPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn } = useAuth();
  const { addToCart } = useCart();

  const [restaurant, setRestaurant] = useState(null);
  const [menu, setMenu] = useState([]);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [rRes, mRes] = await Promise.all([
          restaurantAPI.getById(id),
          menuAPI.getByRestaurant(id),
        ]);
        setRestaurant(rRes.data.data);
        setMenu(mRes.data.data || []);
      } catch {
        toast.error("Failed to load restaurant.");
        navigate("/");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id, navigate]);

  const handleAdd = async (menuItemId, itemName) => {
    if (!isLoggedIn) {
      toast.error("Please sign in to add items.");
      navigate("/login");
      return;
    }
    setAdding(menuItemId);
    try {
      await addToCart(menuItemId, 1);
      toast.success(`${itemName} added to cart! 🛒`);
    } catch {
      toast.error("Could not add item.");
    } finally {
      setAdding(null);
    }
  };

  if (loading) return <div className="spinner-wrapper"><div className="spinner" /></div>;

  return (
    <div className="page-wrapper">
      {/* Back */}
      <button className="btn btn-white btn-sm" style={{ marginBottom: 24 }} onClick={() => navigate("/")}>
        ← Back
      </button>

      {/* Restaurant Header */}
      {restaurant && (
        <div style={{
          background: "#FFD166",
          border: "4px solid #1A1A1A",
          borderRadius: 20,
          boxShadow: "8px 8px 0 #1A1A1A",
          padding: "32px 36px",
          marginBottom: 40,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          flexWrap: "wrap",
          gap: 16,
        }}>
          <div>
            <h1 style={{ fontSize: 32, fontWeight: 800, marginBottom: 6 }}>{restaurant.name}</h1>
            <div className="flex gap-3" style={{ flexWrap: "wrap" }}>
              {restaurant.phone && <span style={{ fontSize: 15, fontWeight: 600 }}>📞 {restaurant.phone}</span>}
              {restaurant.address && <span style={{ fontSize: 15, fontWeight: 600 }}>📍 {restaurant.address.street}</span>}
              {restaurant.address && <span style={{ fontSize: 15, fontWeight: 600 }}>🏷️ {restaurant.address.pincode}</span>}
            </div>
          </div>
          <div style={{ fontSize: 64 }}>🍽️</div>
        </div>
      )}

      {/* Menu title */}
      <div style={{ marginBottom: 32 }}>
        <div className="section-header">
          <h2 className="section-title">Our Menu</h2>
          <div className="title-underline-teal" />
        </div>
      </div>

      {menu.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🍽️</div>
          <h3 className="empty-title">No items available</h3>
          <p className="empty-desc">This restaurant hasn't added menu items yet.</p>
        </div>
      ) : (
        <div className="menu-grid">
          {menu.map((item) => (
            <div key={item.menuItemId} className="menu-item-card">
              <div
                style={{
                  height: 8,
                  backgroundColor: CATEGORY_COLORS[item.category] || "#FFD166",
                  borderBottom: "3px solid #1A1A1A",
                }}
              />
              <div className="menu-item-body">
                <div className="flex items-center justify-between mb-4" style={{ marginBottom: 8 }}>
                  <h3 className="menu-item-name">{item.itemName}</h3>
                  <span className={`badge badge-${item.category?.toLowerCase() === "veg" ? "veg" : item.category?.toLowerCase() === "egg" ? "egg" : "nonveg"}`}>
                    {item.category === "VEG" ? "🟢 Veg" : item.category === "EGG" ? "🥚 Egg" : "🔴 Non-Veg"}
                  </span>
                </div>
                {item.description && (
                  <p className="menu-item-desc">{item.description}</p>
                )}
                <p className="menu-item-price">₹{item.price?.toFixed(2)}</p>
              </div>
              <div className="menu-item-footer">
                <span style={{ fontSize: 13, fontWeight: 600, color: item.available ? "#06D6A0" : "#e53935" }}>
                  {item.available ? "● Available" : "● Unavailable"}
                </span>
                <button
                  className="btn btn-black btn-sm"
                  onClick={() => handleAdd(item.menuItemId, item.itemName)}
                  disabled={!item.available || adding === item.menuItemId}
                >
                  {adding === item.menuItemId ? "Adding…" : "+ Add"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
