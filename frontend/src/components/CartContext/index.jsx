import { createContext, useState, useEffect } from "react";
import { get } from "../../utils/request";

export const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cartVariants, setCartVariants] = useState([]); // chỉ lưu variantId
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!token) return;

    const fetchCartVariants = async () => {
      try {
        const res = await get(`cart-items/count`);

        if (res.ok) {
          const data = await res.json(); 
          setCartVariants(data);
        }
      } catch (error) {
        console.error(error);
      }
    };

    fetchCartVariants();
  }, [token]);


  const addVariant = (variantId) => {
    if (!cartVariants.includes(variantId)) {
      setCartVariants(prev => [...prev, variantId]);
    }
  };

  const removeVariant = (variantId) => {
    if (cartVariants.includes(variantId)) {
      setCartVariants(cartVariants.filter(it => it !== variantId));
    }
  };

  return (
    <CartContext.Provider value={{ cartVariants, addVariant, removeVariant, token }}>
      {children}
    </CartContext.Provider>
  );
};