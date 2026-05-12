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
import { FaStore } from "react-icons/fa";
import { IoChevronUp } from "react-icons/io5";
import { BsTruck } from "react-icons/bs";
import { BiSolidMessageDetail } from "react-icons/bi";
import { useNavigate } from "react-router-dom";
import { message } from "antd";

function Checkout() {
  const [openAddressModal, setOpenAddressModal] = useState(false);
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [note, setNote] = useState("");

  const [shippingMethods, setShippingMethods] = useState([]);
  const [shippingByShop, setShippingByShop] = useState({});
  const [loadingShipping, setLoadingShipping] = useState(false);

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
  const [selectedPayment, setSelectedPayment] = useState(paymentMethods[0] || null);

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

  console.log("checkout: ", checkoutItems)

  const groupedCheckoutItems = useMemo(() => {
    return checkoutItems.reduce((acc, item) => {
      if (!acc[item.shopId]) {
        acc[item.shopId] = [];
      }

      acc[item.shopId].push(item);
      return acc;
    }, {});
  }, [checkoutItems]);

  const subtotal = useMemo(() => {
    return checkoutItems.reduce(
      (sum, item) => sum + item.displayPrice * item.quantity,
      0
    );
  }, [checkoutItems]);

  useEffect(() => {
    fetchAvailableServices();
  }, [selectedAddress, checkoutItems, groupedCheckoutItems]);


  const fetchAvailableServices = async () => {
    if (!selectedAddress || checkoutItems.length === 0) return;

    try {
      setLoadingShipping(true);

      const result = {};

      for (const shopId in groupedCheckoutItems) {
        const shopItems = groupedCheckoutItems[shopId];

        const shopSubtotal = shopItems.reduce(
          (sum, item) => sum + item.displayPrice * item.quantity,
          0
        );

        const res = await post("shipping/available-services", {
          shopId: Number(shopId),
          toDistrictId: selectedAddress.districtId,
          toWardCode: selectedAddress.wardId,
          insuranceValue: shopSubtotal,
          items: shopItems.map(item => ({
            productId: item.productId,
            quantity: item.quantity,
          })),
        });

        const data = await res.json();

        if (!res.ok) {
          throw new Error(data.message || "Không lấy được dịch vụ vận chuyển");
        }

        const methods = data.map(item => ({
          id: item.serviceId,
          name: item.name,
          desc: item.description,
          fee: item.fee,
          serviceTypeId: item.serviceTypeId,
          expectedDeliveryTime: item.expectedDeliveryTime,
        }));

        result[shopId] = {
          methods,
          selected: methods[0] || null,
        };
      }

      setShippingByShop(result);
    } catch (error) {
      message.error(error.message);
      setShippingByShop({});
    } finally {
      setLoadingShipping(false);
    }
  };

  const shopCount = useMemo(() => {
    const uniqueShopIds = new Set(
      checkoutItems
        .map(item => item.shopId)
        .filter(Boolean)
    );

    return uniqueShopIds.size || 1;
  }, [checkoutItems]);

  const shippingFee = useMemo(() => {
    return Object.values(shippingByShop).reduce((sum, item) => {
      return sum + (item.selected?.fee || 0);
    }, 0);
  }, [shippingByShop]);
  const total = subtotal + shippingFee;

  const handlePlaceOrder = async () => {
    if (!selectedAddress) {
      message.error("Vui lòng chọn địa chỉ nhận hàng");
      return;
    }

    const missingShipping = Object.values(shippingByShop)
      .some(item => !item.selected);

    if (
      Object.keys(groupedCheckoutItems).length === 0 ||
      Object.keys(shippingByShop).length !== Object.keys(groupedCheckoutItems).length ||
      missingShipping
    ) {
      message.error("Vui lòng chọn đầy đủ phương thức vận chuyển cho từng shop");
      return;
    }

    const payload = {
      addressId: selectedAddress.id,

      shippingFees: Object.entries(shippingByShop).map(([shopId, item]) => ({
        shopId: Number(shopId),
        shippingServiceId: item.selected?.id,
        shippingServiceName: item.selected?.name,
        fee: item.selected?.fee,
      })),

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

              {loadingShipping && (
                <div className="checkout-shipping__empty">
                  Đang tải dịch vụ vận chuyển...
                </div>
              )}

              {!loadingShipping && !selectedAddress && (
                <div className="checkout-shipping__empty">
                  Vui lòng chọn địa chỉ nhận hàng
                </div>
              )}

              {!loadingShipping && selectedAddress && Object.entries(groupedCheckoutItems).map(([shopId, shopItems], index) => {
                const shopName = shopItems[0]?.shopName || `Shop ${shopId}`;
                const shopLogo = shopItems[0]?.shopLogo
                const selectedId = shippingByShop[shopId]?.selected?.id;
                const methods = shippingByShop[shopId]?.methods || [];

                return (
                  <div key={shopId} className="checkout-shipping__shop-card">
                    <div className="checkout-shipping__shop-header">
                      <div className="checkout-shipping__shop-left">
                        <div className={`checkout-shipping__shop-avatar ${index % 2 === 0 ? "orange" : "green"}`}>
                          {shopLogo ? <img src={shopLogo}/> : <FaStore />}
                        </div>

                        <div>
                          <div className="checkout-shipping__shop-name-row">
                            <span className="checkout-shipping__shop-name">
                              {shopName}
                            </span>
                            {/* <span className={`checkout-shipping__shop-badge ${index % 2 === 0 ? "orange" : "green"}`}>
                              Shop #{index + 1}
                            </span> */}
                          </div>

                          <div className="checkout-shipping__shop-count">
                            {shopItems.length} sản phẩm
                          </div>
                        </div>
                      </div>

                      {/* <IoChevronUp className="checkout-shipping__shop-arrow" /> */}
                    </div>

                    <div className="checkout-shipping__method-list">
                      {methods.map((item, methodIndex) => {
                        const active = selectedId === item.id;

                        return (
                          <button
                            key={item.id}
                            className={`checkout-shipping__method ${active ? "active" : ""}`}
                            onClick={() => {
                              setShippingByShop(prev => ({
                                ...prev,
                                [shopId]: {
                                  ...prev[shopId],
                                  selected: item,
                                },
                              }));
                            }}
                          >
                            <div className={`checkout-shipping__radio ${active ? "active" : ""}`}>
                              {active && <span />}
                            </div>

                            <div className={`checkout-shipping__method-icon ${active ? "active" : ""}`}>
                              <BsTruck />
                            </div>

                            <div className="checkout-shipping__method-content">
                              <div className="checkout-shipping__method-name">
                                {item.name}
                              </div>

                              <div className="checkout-shipping__method-desc">
                                {item.desc}
                              </div>
                            </div>

                            <div className="checkout-shipping__method-price-wrap">
                              <div className="checkout-shipping__method-price">
                                {formatPrice(item.fee)}
                              </div>

                              {/* {methodIndex === 0 && (
                                <div className="checkout-shipping__suggest">
                                  Đề xuất
                                </div>
                              )} */}
                            </div>
                          </button>
                        );
                      })}
                    </div>
                  </div>
                );
              })}

              <div className="checkout-shipping__total">
                <span>Tổng phí vận chuyển</span>
                <strong>{formatPrice(shippingFee)}</strong>
              </div>
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
                  onChange={(e) => setNote(e.target.value)} />
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