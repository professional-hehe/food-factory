import React, { useEffect, useState } from "react";
import { orderAPI } from "../api/services";
import toast from "react-hot-toast";

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(null);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const res = await orderAPI.getAll();
      setOrders(res.data.data || []);
    } catch {
      toast.error("Could not load orders.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchOrders(); }, []);

  const handleCancel = async (orderId) => {
    setCancelling(orderId);
    try {
      await orderAPI.cancel(orderId);
      toast.success("Order cancelled.");
      fetchOrders();
    } catch (err) {
      toast.error(err.response?.data?.message || "Could not cancel order.");
    } finally {
      setCancelling(null);
    }
  };

  if (loading) return <div className="spinner-wrapper"><div className="spinner" /></div>;

  return (
    <div className="page-wrapper">
      <div className="page-title-block">
        <h1 className="page-title">📦 My Orders</h1>
        <p className="page-subtitle">{orders.length} order{orders.length !== 1 ? "s" : ""} placed</p>
      </div>

      {orders.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📦</div>
          <h3 className="empty-title">No orders yet</h3>
          <p className="empty-desc">Place your first order to see it here!</p>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => (
            <div key={order.orderId} className="order-card">
              <div className="order-card-header">
                <div>
                  <div className="order-id">Order #{order.orderId}</div>
                  <div className="order-meta">
                    {order.restaurantName} · {order.paymentType}
                    {order.createdAt && ` · ${new Date(order.createdAt).toLocaleDateString()}`}
                  </div>
                </div>
                <span className={`status-badge status-${order.status}`}>
                  {order.status}
                </span>
              </div>

              <div className="order-card-body">
                {order.items?.map((item) => (
                  <div key={item.menuItemId} className="order-items-row">
                    {item.itemName} × {item.quantity} — ₹{(item.price * item.quantity).toFixed(2)}
                  </div>
                ))}
                <div className="order-total">Total: ₹{Number(order.totalPrice).toFixed(2)}</div>
              </div>

              {order.status === "PLACED" && (
                <div className="order-card-footer">
                  <button
                    className="btn btn-accent1 btn-sm"
                    onClick={() => handleCancel(order.orderId)}
                    disabled={cancelling === order.orderId}
                  >
                    {cancelling === order.orderId ? "Cancelling…" : "Cancel Order"}
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
