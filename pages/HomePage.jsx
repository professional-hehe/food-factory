import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { restaurantAPI } from "../api/services";
import toast from "react-hot-toast";

const BANNER_COLORS = ["#FFD166", "#06D6A0", "#F48FB1", "#D2B4FB", "#B5EAD7", "#FFDAC1"];
const EMOJIS = ["🍛", "🍕", "🍜", "🥗", "🍱", "🌮", "🍔", "🥘"];
const CATEGORY_COLORS = { VEG: "#C8F7D5", NON_VEG: "#FFCDD2", EGG: "#FFD166" };

export default function HomePage() {
  const [restaurants, setRestaurants] = useState([]);
  const [foodResults, setFoodResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [query, setQuery] = useState("");
  const [mode, setMode] = useState("restaurants"); // "restaurants" | "food"
  const navigate = useNavigate();

  const fetchRestaurants = async (q = "") => {
    setLoading(true);
    try {
      const res = await restaurantAPI.getAll(q || undefined);
      setRestaurants(res.data.data || []);
    } catch {
      toast.error("Could not load restaurants.");
    } finally {
      setLoading(false);
    }
  };

  const fetchFood = async (q) => {
    if (!q.trim()) return;
    setLoading(true);
    try {
      const res = await restaurantAPI.searchFood(q);
      setFoodResults(res.data.data || []);
    } catch {
      toast.error("Could not search food items.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchRestaurants(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    setQuery(search);
    if (mode === "restaurants") {
      fetchRestaurants(search);
    } else {
      fetchFood(search);
    }
  };

  const clearSearch = () => {
    setSearch("");
    setQuery("");
    setFoodResults([]);
    fetchRestaurants();
  };

  const handleModeChange = (newMode) => {
    setMode(newMode);
    setSearch("");
    setQuery("");
    setFoodResults([]);
    if (newMode === "restaurants") fetchRestaurants();
  };

  return (
    <div className="page-wrapper">
      {/* Hero */}
      <div className="hero">
        <div className="hero-text">
          <h1 className="hero-title">Hungry? <br />We got you. 😋</h1>
          <p className="hero-subtitle">
            Order from the best restaurants in your city — fast, fresh, and delicious.
          </p>
        </div>
        <div className="hero-emoji">🍽️</div>
      </div>

      {/* Search Mode Toggle */}
      <div style={{ display: "flex", gap: 10, marginBottom: 14 }}>
        <button
          type="button"
          className={`btn btn-sm ${mode === "restaurants" ? "btn-black" : "btn-white"}`}
          onClick={() => handleModeChange("restaurants")}
        >
          🏪 Restaurants
        </button>
        <button
          type="button"
          className={`btn btn-sm ${mode === "food" ? "btn-black" : "btn-white"}`}
          onClick={() => handleModeChange("food")}
        >
          🍔 Food Items
        </button>
      </div>

      {/* Search bar */}
      <form className="search-bar-wrapper" onSubmit={handleSearch}>
        <input
          className="neo-input"
          placeholder={mode === "restaurants" ? "Search restaurants…" : "Search food items (e.g. Butter Chicken)…"}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button type="submit" className="btn btn-black">Search</button>
        {query && (
          <button type="button" className="btn btn-white" onClick={clearSearch}>Clear</button>
        )}
      </form>

      {/* ── RESTAURANT MODE ── */}
      {mode === "restaurants" && (
        <>
          <div style={{ marginBottom: 32 }}>
            <div className="section-header">
              <h2 className="section-title">
                {query ? `Restaurants matching "${query}"` : "All Restaurants"}
              </h2>
              <div className="title-underline" />
            </div>
          </div>

          {loading && <div className="spinner-wrapper"><div className="spinner" /></div>}

          {!loading && restaurants.length === 0 && (
            <div className="empty-state">
              <div className="empty-icon">🔍</div>
              <h3 className="empty-title">No restaurants found</h3>
              <p className="empty-desc">Try a different search term.</p>
              {query && <button className="btn btn-primary" onClick={clearSearch}>Show all</button>}
            </div>
          )}

          {!loading && restaurants.length > 0 && (
            <div className="restaurants-grid">
              {restaurants.map((r, i) => (
                <div
                  key={r.id}
                  className="restaurant-card"
                  onClick={() => navigate(`/restaurant/${r.id}`)}
                >
                  <div
                    className="restaurant-card-banner"
                    style={{ backgroundColor: BANNER_COLORS[i % BANNER_COLORS.length] }}
                  >
                    {EMOJIS[i % EMOJIS.length]}
                  </div>
                  <div className="restaurant-card-body">
                    <div className="restaurant-card-name">{r.name}</div>
                    <div className="restaurant-card-meta">
                      <span>📞 {r.phone || "N/A"}</span>
                      {r.address && <span>📍 {r.address.street}</span>}
                    </div>
                  </div>
                  <div className="restaurant-card-footer">View menu →</div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* ── FOOD ITEMS MODE ── */}
      {mode === "food" && (
        <>
          <div style={{ marginBottom: 32 }}>
            <div className="section-header">
              <h2 className="section-title">
                {query ? `Food items matching "${query}"` : "Search for a dish"}
              </h2>
              <div className="title-underline" />
            </div>
          </div>

          {/* Prompt before first search */}
          {!query && !loading && (
            <div className="empty-state">
              <div className="empty-icon">🔍</div>
              <h3 className="empty-title">What are you craving?</h3>
              <p className="empty-desc">Type a dish name above and hit Search.</p>
            </div>
          )}

          {loading && <div className="spinner-wrapper"><div className="spinner" /></div>}

          {!loading && query && foodResults.length === 0 && (
            <div className="empty-state">
              <div className="empty-icon">😕</div>
              <h3 className="empty-title">No dishes found</h3>
              <p className="empty-desc">Try something like "Biryani", "Pizza" or "Chicken".</p>
              <button className="btn btn-primary" onClick={clearSearch}>Clear</button>
            </div>
          )}

          {!loading && foodResults.length > 0 && (
            <div className="restaurants-grid">
              {foodResults.map((item) => (
                <div
                  key={item.menuItemId}
                  className="restaurant-card"
                  style={{ cursor: "pointer" }}
                  onClick={() => navigate(`/restaurant/${item.restaurantId}`)}
                >
                  <div
                    className="restaurant-card-banner"
                    style={{ backgroundColor: CATEGORY_COLORS[item.category] || "#FFD166" }}
                  >
                    {item.category === "VEG" ? "🥗" : item.category === "EGG" ? "🥚" : "🍗"}
                  </div>
                  <div className="restaurant-card-body">
                    <div className="restaurant-card-name">{item.itemName}</div>
                    {item.description && (
                      <p style={{ fontSize: 13, color: "#555", marginBottom: 6, lineHeight: 1.4 }}>
                        {item.description}
                      </p>
                    )}
                    <div className="restaurant-card-meta" style={{ gap: 8 }}>
                      <span
                        style={{
                          background: CATEGORY_COLORS[item.category] || "#FFD166",
                          border: "2px solid #1A1A1A",
                          borderRadius: 20,
                          padding: "2px 10px",
                          fontSize: 12,
                          fontWeight: 700,
                        }}
                      >
                        {item.category === "VEG" ? "🟢 Veg" : item.category === "EGG" ? "🥚 Egg" : "🔴 Non-Veg"}
                      </span>
                      <span style={{ fontWeight: 700, fontSize: 15 }}>₹{item.price?.toFixed(2)}</span>
                    </div>
                  </div>
                  <div className="restaurant-card-footer" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <span style={{ fontSize: 12, color: "#777" }}>🏪 {item.restaurantName}</span>
                    <span>Order →</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
