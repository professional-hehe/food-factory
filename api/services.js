import api from "./axios";

export const authAPI = {
  register: (data) => api.post("/auth/register", data),
  login:    (data) => api.post("/auth/login", data),
  logout:   ()     => api.post("/auth/logout"),
  me:       ()     => api.get("/auth/me"),
};

export const restaurantAPI = {
  getAll:        (search) => api.get("/restaurants", { params: search ? { search } : {} }),
  getById:       (id)     => api.get(`/restaurants/${id}`),
  searchFood:    (query)  => api.get("/restaurants/food/search", { params: { query } }),
};

export const menuAPI = {
  getByRestaurant: (restaurantId) => api.get(`/menu/restaurant/${restaurantId}`),
};

export const cartAPI = {
  get:     ()                          => api.get("/cart"),
  add:     (menuItemId, quantity)      => api.post("/cart/add", { menuItemId, quantity }),
  update:  (cartItemId, quantity)      => api.put(`/cart/update/${cartItemId}`, null, { params: { quantity } }),
  remove:  (cartItemId)                => api.delete(`/cart/remove/${cartItemId}`),
  clear:   ()                          => api.delete("/cart/clear"),
};

export const orderAPI = {
  place:    (paymentType) => api.post("/orders/place", { paymentType }),
  getAll:   ()            => api.get("/orders"),
  getById:  (id)          => api.get(`/orders/${id}`),
  cancel:   (id)          => api.put(`/orders/${id}/cancel`),
};

export const adminAPI = {
  getAllRestaurants: ()               => api.get("/admin/restaurants"),
  toggleRestaurantStatus: (id, active) => api.put(`/admin/restaurants/${id}/status`, null, { params: { active } }),
  getAllMenuByRestaurant: (rid)         => api.get(`/admin/restaurants/${rid}/menu`),
  toggleMenuAvailability: (mid, available) => api.put(`/admin/menu/${mid}/availability`, null, { params: { available } }),
  repairMenuItems:         ()               => api.put("/admin/menu/repair"),
  addRestaurant:    (data)           => api.post("/admin/restaurants", data),
  updateRestaurant: (id, data)       => api.put(`/admin/restaurants/${id}`, data),
  deleteRestaurant: (id)             => api.delete(`/admin/restaurants/${id}`),
  addMenuItem:      (rid, data)      => api.post(`/admin/restaurants/${rid}/menu`, data),
  updateMenuItem:   (mid, data)      => api.put(`/admin/menu/${mid}`, data),
  deleteMenuItem:   (mid)            => api.delete(`/admin/menu/${mid}`),
  getOrdersByRestaurant: (rid)       => api.get(`/admin/restaurants/${rid}/orders`),
  updateOrderStatus:(oid, status)    => api.put(`/admin/orders/${oid}/status`, null, { params: { status } }),
};
