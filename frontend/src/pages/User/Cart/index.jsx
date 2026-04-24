import "./Cart.scss"
import { FaRegTrashAlt } from "react-icons/fa";
import { CiShop } from "react-icons/ci";
import { Checkbox, message } from "antd"
import { MdKeyboardArrowRight } from "react-icons/md";
import { Link, useNavigate } from "react-router-dom";
import { useContext, useEffect, useMemo, useState } from "react";
import { del, get, put } from "../../../utils/request"
import { CartContext } from "../../../components/CartContext";
import { formatPrice } from "../../../utils/price";


function Cart() {
  const navigate = useNavigate();
  const { cartVariants, removeVariant } = useContext(CartContext);
  const [cart, setCart] = useState([]);
  const [checkedItems, setCheckedItems] = useState({});
  const [subtotal, setSubtotal] = useState(0);
  const [selectedQuantity, setSelectedQuantity] = useState(0);

  useEffect(() => {
    const fetchApi = async () => {
      try {
        const res = await get('cart-items');

        const data = await res.json();

        setCart(data);
      } catch (error) {
        console.log(error);
      }
    }
    fetchApi();
  }, [])

  const handleCheckboxChange = (cartItemId, checked) => {
    setCheckedItems(prev => ({ ...prev, [cartItemId]: checked }));
  };

  const showSummary = Object.values(checkedItems).some(Boolean);

  const updateTotals = () => {
    const selectedItems = cart.flatMap(s => s.cartItems)
      .filter(item => checkedItems[item.cartItemId]);
    const total = selectedItems.reduce((sum, item) => sum + item.displayPrice * item.quantity, 0);
    const qty = selectedItems.reduce((sum, item) => sum + item.quantity, 0);

    setSubtotal(total);
    setSelectedQuantity(qty);
  };

  useEffect(() => {
    updateTotals();
  }, [checkedItems, cart]);

  const handleQuantityChange = async (shopId, cartItemId, newQuantity) => {
    const shop = cart.find(s => s.shopId === shopId);
    const item = shop.cartItems.find(i => i.cartItemId === cartItemId);
    if (!item) return;

    const qty = Math.max(1, Math.min(newQuantity, item.stock));

    setCart(prevCart =>
      prevCart.map(s => {
        if (s.shopId !== shopId) return s;
        return {
          ...s,
          cartItems: s.cartItems.map(ci => {
            if (ci.cartItemId !== cartItemId) return ci;
            return { ...ci, quantity: qty };
          }),
        };
      })
    );

    if (checkedItems[cartItemId]) {
      updateTotals(cartItemId, newQuantity);
    }

    try {
      await put(`cart-items/${cartItemId}`, {
        quantity: qty
      });
    } catch (error) {
      console.error("Cập nhật số lượng thất bại:", error);
    }
  };

  const handleRemoveItem = async (shopId, cartItemId, variantId) => {
    const prevCart = [...cart];

    setCart(prevCart =>
      prevCart
        .map(shop => {
          if (shop.shopId !== shopId) return shop;
          return {
            ...shop,
            cartItems: shop.cartItems.filter(item => item.cartItemId !== cartItemId),
          };
        })
        .filter(shop => shop.cartItems.length > 0)
    );

    try {
      const res = await del(`cart-items/${cartItemId}`);

      if (!res.ok) throw new Error("Server error");

      message.success("Xóa sản phẩm thành công!");
      removeVariant(variantId);

    } catch (error) {
      console.error("Xóa sản phẩm thất bại:", error);
      setCart(prevCart);
      message.error("Xóa sản phẩm thất bại. Vui lòng thử lại.");
    }
  };

  const selectedCartItems = cart.flatMap(shop =>
    shop.cartItems
      .filter(item => checkedItems[item.cartItemId])
      .map(item => ({
        ...item,
        shopId: shop.shopId,
        shopName: shop.shopName,
      }))
  );

  const shopCount = useMemo(() => {
    const uniqueShopIds = new Set(
      selectedCartItems.map(item => item.shopId).filter(Boolean)
    );

    return uniqueShopIds.size;
  }, [selectedCartItems]);

  const shippingFee = useMemo(() => {
    return shopCount * 35000;
  }, [shopCount]);

  const handleCheckout = () => {
    localStorage.setItem("checkoutItems", JSON.stringify(selectedCartItems));
    navigate("/thanh-toan");
  };

  return (
    <>
      <div className="cart">
        <div className="container">
          {cart.length === 0 ? (
            <div className="cart-empty">
              <img
                src="https://res.cloudinary.com/dcjraarbb/image/upload/v1776155613/undraw_empty-cart_574u_siop6o.svg"
                alt="Giỏ hàng trống"
                className="cart-empty__image"
              />
              <h2 className="cart-empty__title">Giỏ hàng của bạn đang trống</h2>
              <p className="cart-empty__desc">
                Hãy thêm sản phẩm yêu thích của bạn vào giỏ hàng.
              </p>
              <Link to="/" className="cart-empty__btn">
                Tiếp tục mua sắm
              </Link>
            </div>
          ) : (
            <>
              <div className="cart__title">
                Giỏ hàng
              </div>
              <div className="cart__subtitle">
                Bạn đang có <span>{cartVariants.length} sản phẩm</span> trong giỏ hàng
              </div>
              <div className="cart__body">
                <div className="cart__list">
                  {cart?.map(shop => (
                    <div className="cart-shop" key={shop.shopId}>
                      <Link to={`/cua-hang/${shop.shopId}`} className="cart-shop__header">
                        {/* <Checkbox /> */}
                        <CiShop />
                        <span className="cart-shop__name">{shop.shopName}</span>
                        <MdKeyboardArrowRight />
                      </Link>

                      {shop.cartItems.map(item => (
                        <div className="cart-item" key={item.cartItemId}>
                          <Checkbox
                            className="cart-item__checkbox"
                            checked={!!checkedItems[item.cartItemId]}
                            onChange={e => handleCheckboxChange(item.cartItemId, e.target.checked)}
                          />
                          <img className="cart-item__image" src={item.imageUrl} alt="" />

                          <Link to={`/san-pham/${item.productId}`} className="cart-item__info">
                            <h4 className="cart-item__name">{item.productName}</h4>
                            <div className="cart-item__variant">Phân loại: {item.color}, {item.size}</div>
                          </Link>

                          {item.stock === 0 ? (
                            <div className="cart-item__sold-out">
                              Loại hàng đã chọn không còn
                            </div>
                          ) : (
                            <>
                              <div className="cart-item__price">{formatPrice(item.displayPrice)}</div>

                              <div className="cart-item__quantity">
                                <button
                                  className="cart-item__quantity-btn "
                                  onClick={() =>
                                    handleQuantityChange(shop.shopId, item.cartItemId, item.quantity - 1)
                                  }
                                >
                                  -
                                </button>
                                <input
                                  className="cart-item__quantity-input"
                                  value={item.quantity}
                                  type="number"
                                  min={1}
                                  max={item.stock}
                                  onChange={e =>
                                    handleQuantityChange(
                                      shop.shopId,
                                      item.cartItemId,
                                      parseInt(e.target.value) || 1
                                    )
                                  }
                                />
                                <button
                                  className="cart-item__quantity-btn "
                                  onClick={() =>
                                    handleQuantityChange(shop.shopId, item.cartItemId, item.quantity + 1)
                                  }
                                >
                                  +
                                </button>
                              </div>

                              <div className="cart-item__total">{formatPrice(item.displayPrice * item.quantity)}</div>
                            </>
                          )}

                          <button className="cart-item__remove" onClick={() => handleRemoveItem(shop.shopId, item.cartItemId, item.variantId)}>
                            <FaRegTrashAlt />
                          </button>
                        </div>
                      ))}

                    </div>
                  ))}
                </div>

                {Object.values(checkedItems).some(Boolean) && (
                  <div className="cart-summary">
                    <h2 className="cart-summary__title">Tóm tắt đơn hàng</h2>

                    <div className="cart-summary__row">
                      <span>Tạm tính ({selectedQuantity} sản phẩm) </span>
                      <span className="cart-summary__value">{formatPrice(subtotal)}</span>
                    </div>

                    <div className="cart-summary__row">
                      <span>Phí vận chuyển {shopCount > 1 ? `(${shopCount} shop)` : ""}</span>
                      <span className="cart-summary__value ">{formatPrice(shippingFee)}</span>
                    </div>
                    <div style={{ opacity: 0.6, color: "red" }}>Phí vận chuyển tạm tính, sẽ được xác nhận tại bước thanh toán.</div>

                    <div className="cart-summary__total">
                      <div className="cart-summary__total-title">Tổng thanh toán</div>
                      <div className="cart-summary__total-content">
                        <h2 className="cart-summary__total-price">{formatPrice(subtotal + shippingFee)}</h2>
                        <div className="cart-summary__total-desc">
                          (ĐÃ BAO GỒM VAT)
                        </div>
                      </div>
                    </div>

                    <button className="cart-summary__buy" onClick={handleCheckout}>
                      Mua hàng ({selectedQuantity})
                    </button>
                  </div>
                )}

              </div>
            </>

          )}

        </div>
      </div>
    </>
  )
}

export default Cart;