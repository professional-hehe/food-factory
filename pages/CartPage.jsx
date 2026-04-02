import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { orderAPI } from "../api/services";
import toast from "react-hot-toast";

export default function CartPage() {
  const { cart, loading, fetchCart, updateItem, removeItem, clearCart } = useCart();
  const [paymentType, setPaymentType] = useState("COD");
  const [placing, setPlacing] = useState(false);
  const navigate = useNavigate();

  useEffect(() => { fetchCart(); }, [fetchCart]);

  const handlePlaceOrder = async () => {
    if (!cart?.items?.length) { toast.error("Your cart is empty!"); return; }
    setPlacing(true);
    try {
      await orderAPI.place(paymentType);
      await clearCart();
      toast.success("Order placed! 🎉");
      navigate("/orders");
    } catch (err) {
      toast.error(err.response?.data?.message || "Could not place order.");
    } finally {
      setPlacing(false);
    }
  };

  if (loading) return <div className="spinner-wrapper"><div className="spinner" /></div>;

  const items = cart?.items || [];
  const total = cart?.totalPrice ?? items.reduce((s, i) => s + i.price * i.quantity, 0);

  if (items.length === 0) {
    return (
      <div className="page-wrapper">
        <div className="empty-state">
          <div className="empty-icon">🛒</div>
          <h3 className="empty-title">Your cart is empty</h3>
          <p className="empty-desc">Add some delicious items from a restaurant!</p>
          <button className="btn btn-primary btn-lg" onClick={() => navigate("/")}>
            Browse Restaurants
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page-wrapper">
      <div className="page-title-block">
        <h1 className="page-title">🛒 Your Cart</h1>
        <p className="page-subtitle">{items.length} item{items.length !== 1 ? "s" : ""} in your cart</p>
      </div>

      <div className="cart-layout">
        {/* Items */}
        <div>
          <div className="cart-items-list">
            {items.map((item) => (
              <div key={item.cartItemId} className="cart-item-card">
                <div className="cart-item-info">
                  <div className="cart-item-name">{item.itemName}</div>
                  <div className="cart-item-price">₹{item.price?.toFixed(2)} each</div>
                </div>

                <div className="qty-control">
                  <button className="qty-btn" onClick={() => updateItem(item.cartItemId, item.quantity - 1)} disabled={item.quantity <= 1}>−</button>
                  <span className="qty-value">{item.quantity}</span>
                  <button className="qty-btn" onClick={() => updateItem(item.cartItemId, item.quantity + 1)}>+</button>
                </div>

                <div style={{ fontWeight: 800, minWidth: 70, textAlign: "right" }}>
                  ₹{(item.price * item.quantity).toFixed(2)}
                </div>

                <button className="remove-btn" onClick={() => removeItem(item.cartItemId)} title="Remove">✕</button>
              </div>
            ))}
          </div>

          <button
            className="btn btn-white btn-sm"
            style={{ marginTop: 16 }}
            onClick={() => { clearCart(); toast.success("Cart cleared"); }}
          >
            🗑 Clear cart
          </button>
        </div>

        {/* Summary */}
        <div className="cart-summary">
          <h2 className="cart-summary-title">Order Summary</h2>

          {items.map((item) => (
            <div key={item.cartItemId} className="summary-row">
              <span>{item.itemName} × {item.quantity}</span>
              <span>₹{(item.price * item.quantity).toFixed(2)}</span>
            </div>
          ))}

          <div className="summary-total">
            <span>Total</span>
            <span>₹{Number(total).toFixed(2)}</span>
          </div>

          <div className="form-group" style={{ marginBottom: 20 }}>
            <label className="form-label">Payment Method</label>
            <select
              className="neo-select"
              value={paymentType}
              onChange={(e) => setPaymentType(e.target.value)}
            >
              <option value="COD">💵 Cash on Delivery</option>
              <option value="ONLINE">💳 Online Payment</option>
            </select>
          </div>

          <button
            className="btn btn-secondary btn-full btn-lg"
            onClick={handlePlaceOrder}
            disabled={placing}
          >
            {placing ? "Placing order…" : "Place Order 🎉"}
          </button>
        </div>
      </div>
    </div>
  );
}
