import "./Checkout.scss";
import { FaLocationDot } from "react-icons/fa6";
import { MdLocalShipping } from "react-icons/md";
import { MdPayments } from "react-icons/md";
import { SiPayhip } from "react-icons/si";
import { MdHandshake } from "react-icons/md";
import { MdShoppingBag } from "react-icons/md";
import AddressModal from "./AddressModal";
import { useEffect, useMemo, useState } from "react";
import { get, post } from "../../../utils/request";
import { formatPrice } from "../../../utils/price";
import { BiSolidMessageDetail } from "react-icons/bi";
import { useNavigate } from "react-router-dom";
import { message } from "antd";

function Checkout() {
  const [openAddressModal, setOpenAddressModal] = useState(false);
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [note, setNote] = useState("");

  const shippingMethods = [
    {
      id: "fast",
      name: "Giao hàng nhanh",
      desc: "Dự kiến nhận hàng sau: 2 - 3 ngày",
      fee: 35000,
    },
    {
      id: "economy",
      name: "Giao hàng tiết kiệm",
      desc: "Dự kiến nhận hàng sau: 4 - 5 ngày",
      fee: 15000,
    },
  ];

  const paymentMethods = [
    {
      id: "vnpay",
      name: "VNPay",
      icon: <SiPayhip />,
    },
    {
      id: "cod",
      name: "Khi nhận hàng",
      icon: <MdHandshake />,
    },
  ];
  const [selectedShipping, setSelectedShipping] = useState(shippingMethods[0]);
  const [selectedPayment, setSelectedPayment] = useState(paymentMethods[0]);

  const fetchAddresses = async () => {
    try {
      const res = await get("addresses");
      const data = await res.json();

      setAddresses(data);

      setSelectedAddress((prev) => {
        if (prev) {
          const updatedSelected = data.find(item => item.id === prev.id);
          if (updatedSelected) return updatedSelected;
        }
        return data.find(item => item.default) || data[0] || null;
      });
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    fetchAddresses();
  }, []);

  const otherAddresses = useMemo(() => {
    if (!selectedAddress) return addresses;
    return addresses.filter(item => item.id !== selectedAddress.id);
  }, [addresses, selectedAddress]);

  const navigate = useNavigate();
  const [checkoutItems, setCheckoutItems] = useState([]);

  useEffect(() => {
    const savedCheckoutItems = localStorage.getItem("checkoutItems");
    if (savedCheckoutItems) {
      setCheckoutItems(JSON.parse(savedCheckoutItems));
      // localStorage.removeItem("checkoutItems");
    } else {
      navigate("/gio-hang");
    }
  }, [navigate]);

  const subtotal = useMemo(() => {
    return checkoutItems.reduce(
      (sum, item) => sum + item.displayPrice * item.quantity,
      0
    );
  }, [checkoutItems]);

  const shopCount = useMemo(() => {
    const uniqueShopIds = new Set(
      checkoutItems
        .map(item => item.shopId)
        .filter(Boolean)
    );

    return uniqueShopIds.size || 1;
  }, [checkoutItems]);

  const shippingFee = selectedShipping.fee * shopCount;
  const total = subtotal + shippingFee;

  const handlePlaceOrder = async () => {
    console.log("ok");
    if (!selectedAddress) {
      message.error("Vui lòng chọn địa chỉ nhận hàng");
      return;
    }

    const payload = {
      addressId: selectedAddress.id,
      shippingMethod: selectedShipping.id,
      paymentMethod: selectedPayment.id,
      items: checkoutItems.map((item) => ({
        cartItemId: item.cartItemId || null,
        variantId: item.variantId,
        quantity: item.quantity,
      })),
      note
    };

    console.log("ORDER PAYLOAD:", payload);

    try {
      const res = await post("orders", payload);
      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.message || "Đặt hàng thất bại");
      }

      localStorage.removeItem("checkoutItems");

      if (data.paymentMethod === "COD") {
        navigate("/dat-hang-thanh-cong", {
          state: {
            order: {
              id: data.id,
              code: data.code,
              finalTotal: data.finalTotal,
            },
          },
        });
        return;
      }

      if (data.paymentMethod === "VNPAY") {
        window.location.href = data.paymentUrl;
      }
    } catch (error) {
      message.error(error.message);
    }
  };

  return (
    <>
      <div className="checkout">
        <div className="container">
          <div className="checkout__left">
            <div className="checkout-address">
              <div className="checkout__header">
                <div className="checkout__title">
                  <FaLocationDot />
                  <span>Địa chỉ nhận hàng</span>
                </div>
                <div
                  className="checkout-address__change"
                  onClick={() => setOpenAddressModal(true)}
                >
                  Thay đổi
                </div>
              </div>

              <div className="checkout-address__body">
                {selectedAddress ? (
                  <>
                    <div className="checkout-address__body__info">
                      <div className="checkout-address__body__name">
                        {selectedAddress.receiverName}
                      </div>
                      <div className="checkout-address__body__phone">
                        {selectedAddress.phone}
                      </div>
                    </div>
                    <div className="checkout-address__body__content">
                      {selectedAddress.address}
                    </div>
                  </>
                ) : (
                  <div className="checkout-address__body__content">
                    Bạn chưa có địa chỉ nhận hàng
                  </div>
                )}
              </div>
            </div>

            <div className="checkout-shipping">
              <div className="checkout__title">
                <MdLocalShipping />
                <span>Phương thức vận chuyển</span>
              </div>

              {shippingMethods.map((item) => (
                <button
                  key={item.id}
                  className={`checkout-shipping__item ${selectedShipping.id === item.id ? "active" : ""
                    }`}
                  onClick={() => setSelectedShipping(item)}
                >
                  <div className="checkout-shipping__item__content">
                    <div className="checkout-shipping__item__title">
                      {item.name}
                    </div>
                    <div className="checkout-shipping__item__desc">
                      {item.desc}
                    </div>
                  </div>
                  <div className="checkout-shipping__item__price">
                    {formatPrice(item.fee)}
                  </div>
                </button>
              ))}
            </div>

            <div className="checkout-payment">
              <div className="checkout__title">
                <MdPayments />
                <span>Phương thức thanh toán</span>
              </div>

              <div className="checkout-payment__list">
                {paymentMethods.map((item) => (
                  <button
                    key={item.id}
                    className={`checkout-payment__item ${selectedPayment.id === item.id ? "active" : ""
                      }`}
                    onClick={() => setSelectedPayment(item)}
                  >
                    <div className="checkout-payment__item__icon">
                      {item.icon}
                    </div>
                    <div className="checkout-payment__item__title">
                      {item.name}
                    </div>
                  </button>
                ))}
              </div>
            </div>

            <div className="checkout-note">
              <div className="checkout__title">
                <BiSolidMessageDetail />
                <span>Ghi chú cho người bán</span>
              </div>
              <div className="checkout-note__content">
                <textarea className="checkout-note__input" 
                rows={3}
                placeholder="Nhập ghi chú cho shop (ví dụ: giao giờ hành chính...)"
                onChange={(e) => setNote(e.target.value)}/>
              </div>
            </div>
          </div>

          <div className="checkout-summary">
            <div className="checkout-summary__products">
              <div className="checkout-summary__title">
                Sản phẩm thanh toán
              </div>

              {checkoutItems.map(item => (
                <div className="checkout-summary__product" key={item.cartItemId}>
                  <img
                    src={item.imageUrl}
                    alt=""
                    className="checkout-summary__product__image"
                  />
                  <div className="checkout-summary__product__info">
                    <div className="checkout-summary__product__title">
                      {item.productName}
                    </div>
                    <div className="checkout-summary__product__row">
                      <div className="checkout-summary__product__quantity">
                        Số lượng: {item.quantity}
                      </div>
                      <div className="checkout-summary__product__price">
                        {formatPrice(item.displayPrice * item.quantity)}
                      </div>
                    </div>
                    <div className="checkout-summary__product__variant">
                      Phân loại: {item.color}, {item.size}
                    </div>
                  </div>
                </div>
              ))}
              {/* <div className="checkout-summary__product">
                <img
                  src="https://down-vn.img.susercontent.com/file/vn-11134207-7ras8-m1dko69hscig04@resize_w900_nl.webp" alt="" className="checkout-summary__product__image"
                />
                <div className="checkout-summary__product__info">
                  <div className="checkout-summary__product__title">
                    Aó khoác bomber dù hai lớp nam nữ JULIDO trượt nước
                  </div>
                  <div className="checkout-summary__product__row">
                    <div className="checkout-summary__product__quantity">
                      Số lượng: 1
                    </div>
                    <div className="checkout-summary__product__price">
                      121.000đ
                    </div>
                  </div>
                </div>
              </div>
              <div className="checkout-summary__product">
                <img
                  src="https://down-vn.img.susercontent.com/file/vn-11134207-7ras8-m1dko69hscig04@resize_w900_nl.webp" alt="" className="checkout-summary__product__image"
                />
                <div className="checkout-summary__product__info">
                  <div className="checkout-summary__product__title">
                    Aó khoác bomber dù hai lớp nam nữ JULIDO trượt nước
                  </div>
                  <div className="checkout-summary__product__row">
                    <div className="checkout-summary__product__quantity">
                      Số lượng: 1
                    </div>
                    <div className="checkout-summary__product__price">
                      121.000đ
                    </div>
                  </div>
                </div>
              </div> */}
            </div>

            <div className="checkout-summary__details">
              <div className="checkout-summary__details__title">
                Chi tiết thanh toán
              </div>

              <div className="checkout-summary__details__list">
                <div className="checkout-summary__detail">
                  <div className="checkout-summary__detail__title">
                    Tổng tiền hàng
                  </div>
                  <div className="checkout-summary__detail__price">
                    {formatPrice(subtotal)}
                  </div>
                </div>
                <div className="checkout-summary__detail">
                  <div className="checkout-summary__detail__title">
                    Phí vận chuyển {shopCount > 1 ? `(${shopCount} shop)` : ""}
                  </div>
                  <div className="checkout-summary__detail__price">
                    {formatPrice(shippingFee)}
                  </div>
                </div>

                {/* <div className="checkout-summary__detail discount">
                  <div className="checkout-summary__detail__title">
                    Giảm giá phí vận chuyển
                  </div>
                  <div className="checkout-summary__detail__price">
                    -35.000đ
                  </div>
                </div> */}
              </div>
            </div>

            <div className="checkout-summary__total">
              <div className="checkout-summary__total-label">
                Tổng thanh toán
              </div>
              <div className="checkout-summary__total-amount">
                {formatPrice(total)}
              </div>
            </div>

            <button
              className="checkout-summary__btn"
              onClick={handlePlaceOrder}
              disabled={checkoutItems.length === 0}
            >
              <MdShoppingBag />
              <span>
                {selectedPayment.id === "vnpay"
                  ? "Thanh toán với VNPay"
                  : "Đặt hàng ngay"}
              </span>
            </button>
          </div>
        </div>
      </div>

      <AddressModal
        open={openAddressModal}
        onCancel={() => setOpenAddressModal(false)}
        selectedAddress={selectedAddress}
        otherAddresses={otherAddresses}
        onSelectAddress={(address) => {
          setSelectedAddress(address);
          setOpenAddressModal(false);
        }}
        onReloadAddresses={fetchAddresses}
      />
    </>
  )
}

export default Checkout;