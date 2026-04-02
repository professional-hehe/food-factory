import React, { useEffect, useState, useCallback } from "react";
import { adminAPI, restaurantAPI } from "../api/services";
import toast from "react-hot-toast";

// ── Small Modal ─────────────────────────────────────────────
function Modal({ title, onClose, children }) {
  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-box">
        <div className="flex items-center justify-between" style={{ marginBottom: 20 }}>
          <h2 className="modal-title" style={{ margin: 0 }}>{title}</h2>
          <button className="btn btn-white btn-sm" onClick={onClose}>✕</button>
        </div>
        {children}
      </div>
    </div>
  );
}

// ── Restaurants Tab ─────────────────────────────────────────
function RestaurantsTab() {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ name: "", phone: "", street: "", pincode: "" });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await adminAPI.getAllRestaurants();
      setRestaurants(res.data.data || []);
    } finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const openAdd = () => { setEditing(null); setForm({ name: "", phone: "", owner: "", street: "", pincode: "" }); setShowModal(true); };
  const openEdit = (r) => {
    setEditing(r.id);
    setForm({ name: r.name, phone: r.phone || "", owner: r.owner || "", street: r.address?.street || "", pincode: r.address?.pincode || "" });
    setShowModal(true);
  };

  const handleSave = async () => {
    try {
      const payload = { name: form.name, phone: form.phone, owner: form.owner, address: { street: form.street, pincode: form.pincode } };
      if (editing) {
        await adminAPI.updateRestaurant(editing, payload);
        toast.success("Restaurant updated!");
      } else {
        await adminAPI.addRestaurant(payload);
        toast.success("Restaurant added!");
      }
      setShowModal(false);
      load();
    } catch (err) { toast.error(err.response?.data?.message || "Save failed."); }
  };

  const handleToggleStatus = async (id, currentActive) => {
    const newActive = !currentActive;
    const label = newActive ? "Activate" : "Deactivate";
    if (!window.confirm(`${label} this restaurant?`)) return;
    try {
      await adminAPI.toggleRestaurantStatus(id, newActive);
      toast.success(`Restaurant ${newActive ? "activated" : "deactivated"}.`);
      load();
    } catch { toast.error("Failed to update status."); }
  };

  return (
    <div>
      <div className="flex items-center justify-between" style={{ marginBottom: 20 }}>
        <h2 style={{ fontSize: 22, fontWeight: 800 }}>Restaurants</h2>
        <button className="btn btn-primary btn-sm" onClick={openAdd}>+ Add Restaurant</button>
      </div>

      {loading ? <div className="spinner-wrapper"><div className="spinner" /></div> : (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th><th>Name</th><th>Phone</th><th>Location</th><th>Status</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {restaurants.map((r) => (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td style={{ fontWeight: 700 }}>{r.name}</td>
                  <td>{r.phone || "—"}</td>
                  <td>{r.address?.street || "—"}</td>
                  <td>
                    <span className={`status-badge ${r.active ? "status-CONFIRMED" : "status-CANCELLED"}`}>
                      {r.active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td>
                    <div className="flex gap-2">
                      <button className="btn btn-white btn-sm" onClick={() => openEdit(r)}>Edit</button>
                      <button
                        className={`btn btn-sm ${r.active ? "btn-accent1" : "btn-secondary"}`}
                        onClick={() => handleToggleStatus(r.id, r.active)}
                      >
                        {r.active ? "Deactivate" : "Activate"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <Modal title={editing ? "Edit Restaurant" : "Add Restaurant"} onClose={() => setShowModal(false)}>
          <div className="modal-form">
            {[["name","Restaurant Name","e.g. Spice Garden"], ["phone","Phone","9876543210"], ["owner","Owner","John Doe"], ["street","Street","MG Road, Bangalore"], ["pincode","Pincode","560001"]].map(([key, label, placeholder]) => (
              <div className="form-group" key={key}>
                <label className="form-label">{label}</label>
                <input className="neo-input" placeholder={placeholder} value={form[key]} onChange={(e) => setForm(p => ({ ...p, [key]: e.target.value }))} />
              </div>
            ))}
            <div className="modal-footer">
              <button className="btn btn-white" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-secondary" onClick={handleSave}>Save</button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}

// ── Menu Tab ────────────────────────────────────────────────
function MenuTab() {
  const [restaurants, setRestaurants] = useState([]);
  const [selectedRid, setSelectedRid] = useState("");
  const [menu, setMenu] = useState([]);
  const [loadingMenu, setLoadingMenu] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState({ itemName: "", description: "", price: "", category: "VEG" });

  useEffect(() => {
    restaurantAPI.getAll().then(r => setRestaurants(r.data.data || []));
  }, []);

  const loadMenu = async (rid) => {
    setSelectedRid(rid);
    if (!rid) { setMenu([]); return; }
    setLoadingMenu(true);
    try {
      const res = await adminAPI.getAllMenuByRestaurant(rid);
      setMenu(res.data.data || []);
    } finally { setLoadingMenu(false); }
  };

  const openAdd = () => { setEditing(null); setForm({ itemName: "", description: "", price: "", category: "VEG" }); setShowModal(true); };
  const openEdit = (item) => {
    setEditing(item.menuItemId);
    setForm({ itemName: item.itemName, description: item.description || "", price: item.price, category: item.category });
    setShowModal(true);
  };

  const handleSave = async () => {
    try {
      const payload = { ...form, price: parseFloat(form.price) };
      if (editing) {
        await adminAPI.updateMenuItem(editing, payload);
        toast.success("Menu item updated!");
      } else {
        await adminAPI.addMenuItem(selectedRid, payload);
        toast.success("Menu item added!");
      }
      setShowModal(false);
      loadMenu(selectedRid);
    } catch (err) { toast.error(err.response?.data?.message || "Save failed."); }
  };

  const handleRepair = async () => {
    try {
      await adminAPI.repairMenuItems();
      toast.success("All items set to available!");
      if (selectedRid) loadMenu(selectedRid);
    } catch { toast.error("Repair failed."); }
  };

  const handleToggleAvailability = async (mid, currentAvailable) => {
    const newVal = !currentAvailable;
    try {
      await adminAPI.toggleMenuAvailability(mid, newVal);
      toast.success(`Item marked as ${newVal ? "available" : "unavailable"}.`);
      loadMenu(selectedRid);
    } catch { toast.error("Failed to update availability."); }
  };

  const handleDelete = async (mid) => {
    if (!window.confirm("Remove this menu item?")) return;
    try { await adminAPI.deleteMenuItem(mid); toast.success("Removed."); loadMenu(selectedRid); }
    catch { toast.error("Failed."); }
  };

  return (
    <div>
      <div className="flex items-center justify-between" style={{ marginBottom: 20 }}>
        <h2 style={{ fontSize: 22, fontWeight: 800 }}>Menu Items</h2>
        <div className="flex gap-2">
          <button className="btn btn-white btn-sm" onClick={handleRepair} title="Fix all unavailable items">🔧 Fix Data</button>
          {selectedRid && <button className="btn btn-primary btn-sm" onClick={openAdd}>+ Add Item</button>}
        </div>
      </div>

      <div className="form-group" style={{ maxWidth: 340, marginBottom: 24 }}>
        <label className="form-label">Select Restaurant</label>
        <select className="neo-select" value={selectedRid} onChange={(e) => loadMenu(e.target.value)}>
          <option value="">-- Choose a restaurant --</option>
          {restaurants.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
      </div>

      {!selectedRid && (
        <div className="empty-state">
          <div className="empty-icon">🏪</div>
          <p className="empty-desc">Select a restaurant to manage its menu.</p>
        </div>
      )}

      {selectedRid && loadingMenu && <div className="spinner-wrapper"><div className="spinner" /></div>}

      {selectedRid && !loadingMenu && menu.length === 0 && (
        <div className="empty-state">
          <div className="empty-icon">🍽️</div>
          <p className="empty-desc">No menu items yet. Add one!</p>
        </div>
      )}

      {selectedRid && !loadingMenu && menu.length > 0 && (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr><th>ID</th><th>Item Name</th><th>Category</th><th>Price</th><th>Available</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {menu.map((item) => (
                <tr key={item.menuItemId}>
                  <td>{item.menuItemId}</td>
                  <td style={{ fontWeight: 700 }}>{item.itemName}</td>
                  <td><span className={`badge badge-${item.category?.toLowerCase() === "veg" ? "veg" : item.category?.toLowerCase() === "egg" ? "egg" : "nonveg"}`}>{item.category}</span></td>
                  <td>₹{item.price?.toFixed(2)}</td>
                  <td>
                    <button
                      className={`btn btn-sm ${item.available ? "btn-secondary" : "btn-white"}`}
                      style={{ minWidth: 90 }}
                      onClick={() => handleToggleAvailability(item.menuItemId, item.available)}
                    >
                      {item.available ? "✓ Available" : "✗ Unavailable"}
                    </button>
                  </td>
                  <td>
                    <div className="flex gap-2">
                      <button className="btn btn-white btn-sm" onClick={() => openEdit(item)}>Edit</button>
                      <button className="btn btn-accent1 btn-sm" onClick={() => handleDelete(item.menuItemId)}>Remove</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <Modal title={editing ? "Edit Menu Item" : "Add Menu Item"} onClose={() => setShowModal(false)}>
          <div className="modal-form">
            <div className="form-group">
              <label className="form-label">Item Name</label>
              <input className="neo-input" placeholder="Butter Chicken" value={form.itemName} onChange={e => setForm(p => ({ ...p, itemName: e.target.value }))} />
            </div>
            <div className="form-group">
              <label className="form-label">Description</label>
              <input className="neo-input" placeholder="Creamy tomato curry..." value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} />
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
              <div className="form-group">
                <label className="form-label">Price (₹)</label>
                <input className="neo-input" type="number" placeholder="280" value={form.price} onChange={e => setForm(p => ({ ...p, price: e.target.value }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Category</label>
                <select className="neo-select" value={form.category} onChange={e => setForm(p => ({ ...p, category: e.target.value }))}>
                  <option value="VEG">🟢 Veg</option>
                  <option value="NON_VEG">🔴 Non-Veg</option>
                  <option value="EGG">🥚 Egg</option>
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-white" onClick={() => setShowModal(false)}>Cancel</button>
              <button className="btn btn-secondary" onClick={handleSave}>Save</button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}

// ── Orders Tab ──────────────────────────────────────────────
function OrdersTab() {
  const [restaurants, setRestaurants] = useState([]);
  const [selectedRid, setSelectedRid] = useState("");
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [updating, setUpdating] = useState(null);

  useEffect(() => {
    restaurantAPI.getAll().then(r => setRestaurants(r.data.data || []));
  }, []);

  const loadOrders = async (rid) => {
    setSelectedRid(rid);
    if (!rid) { setOrders([]); return; }
    setLoading(true);
    try {
      const res = await adminAPI.getOrdersByRestaurant(rid);
      setOrders(res.data.data || []);
    } finally { setLoading(false); }
  };

  const handleStatusUpdate = async (orderId, status) => {
    setUpdating(orderId);
    try {
      await adminAPI.updateOrderStatus(orderId, status);
      toast.success(`Status updated to ${status}`);
      loadOrders(selectedRid);
    } catch { toast.error("Failed to update status."); }
    finally { setUpdating(null); }
  };

  const STATUSES = ["PLACED", "CONFIRMED", "DELIVERED", "CANCELLED"];

  return (
    <div>
      <h2 style={{ fontSize: 22, fontWeight: 800, marginBottom: 20 }}>Manage Orders</h2>

      <div className="form-group" style={{ maxWidth: 340, marginBottom: 24 }}>
        <label className="form-label">Select Restaurant</label>
        <select className="neo-select" value={selectedRid} onChange={(e) => loadOrders(e.target.value)}>
          <option value="">-- Choose a restaurant --</option>
          {restaurants.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
        </select>
      </div>

      {!selectedRid && (
        <div className="empty-state">
          <div className="empty-icon">📦</div>
          <p className="empty-desc">Select a restaurant to view its orders.</p>
        </div>
      )}

      {selectedRid && loading && <div className="spinner-wrapper"><div className="spinner" /></div>}

      {selectedRid && !loading && orders.length === 0 && (
        <div className="empty-state">
          <div className="empty-icon">📭</div>
          <p className="empty-desc">No orders for this restaurant yet.</p>
        </div>
      )}

      {selectedRid && !loading && orders.length > 0 && (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr><th>Order ID</th><th>Customer</th><th>Items</th><th>Total</th><th>Payment</th><th>Status</th><th>Update Status</th></tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.orderId}>
                  <td style={{ fontWeight: 700 }}>#{order.orderId}</td>
                  <td>{order.userEmail || "—"}</td>
                  <td>{order.items?.map(i => `${i.itemName} ×${i.quantity}`).join(", ")}</td>
                  <td>₹{Number(order.totalPrice).toFixed(2)}</td>
                  <td>{order.paymentType}</td>
                  <td><span className={`status-badge status-${order.status}`}>{order.status}</span></td>
                  <td>
                    <select
                      className="neo-select"
                      style={{ fontSize: 13, padding: "6px 12px" }}
                      value={order.status}
                      onChange={(e) => handleStatusUpdate(order.orderId, e.target.value)}
                      disabled={updating === order.orderId}
                    >
                      {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

// ── Main Admin Page ─────────────────────────────────────────
export default function AdminPage() {
  const [tab, setTab] = useState("restaurants");

  return (
    <div className="page-wrapper">
      <div className="page-title-block">
        <h1 className="page-title">⚙️ Admin Dashboard</h1>
        <p className="page-subtitle">Manage restaurants, menus, and orders.</p>
      </div>

      <div className="admin-tabs">
        {[["restaurants","🏪 Restaurants"], ["menu","🍽️ Menu"], ["orders","📦 Orders"]].map(([key, label]) => (
          <button
            key={key}
            className={`admin-tab${tab === key ? " active" : ""}`}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === "restaurants" && <RestaurantsTab />}
      {tab === "menu"        && <MenuTab />}
      {tab === "orders"      && <OrdersTab />}
    </div>
  );
}
