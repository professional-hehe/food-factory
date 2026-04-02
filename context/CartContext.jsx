import React, { createContext, useContext, useState, useCallback } from "react";
import { cartAPI } from "../api/services";
import { useAuth } from "./AuthContext";

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isLoggedIn } = useAuth();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchCart = useCallback(async () => {
    if (!isLoggedIn) return;
    try {
      setLoading(true);
      const res = await cartAPI.get();
      setCart(res.data.data);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  }, [isLoggedIn]);

  const addToCart = async (menuItemId, quantity = 1) => {
    const res = await cartAPI.add(menuItemId, quantity);
    setCart(res.data.data);
    return res.data.data;
  };

  const updateItem = async (cartItemId, quantity) => {
    const res = await cartAPI.update(cartItemId, quantity);
    setCart(res.data.data);
  };

  const removeItem = async (cartItemId) => {
    const res = await cartAPI.remove(cartItemId);
    setCart(res.data.data);
  };

  const clearCart = async () => {
    await cartAPI.clear();
    setCart(null);
  };

  const itemCount = cart?.items?.reduce((s, i) => s + i.quantity, 0) || 0;

  return (
    <CartContext.Provider value={{ cart, loading, fetchCart, addToCart, updateItem, removeItem, clearCart, itemCount }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
