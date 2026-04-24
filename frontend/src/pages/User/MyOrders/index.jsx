import "./MyOrders.scss";
import { IoSearch } from "react-icons/io5";
import { MdLocalShipping } from "react-icons/md";
import { FaRegClock, FaCheckCircle, FaTimesCircle } from "react-icons/fa";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { get, put } from "../../../utils/request";
import { formatPrice2 } from "../../../utils/price";
import { connectSocket, subscribeSocket, unsubscribe } from "../../../utils/socket";
import { message, Modal, notification } from "antd";
import ReviewModal from "../ReviewModal";

function MyOrders() {
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [orders, setOrders] = useState([]);

  const [reviewOpen, setReviewOpen] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedOrderId, setSelectedOrderId] = useState(null);

  const fetchOrders = async () => {
    try {
      const res = await get('orders/my', {
        keyword,
        status
      })

      const data = await res.json();
      setOrders(data);

    } catch (error) {
      console.error(error)
    }
  }

  useEffect(() => {
    fetchOrders();
  }, [keyword, status])

  useEffect(() => {
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket("/topic/orders", (data) => {
        if (!data) return;

        if (
          data.type === "ORDER_UPDATED_STATUS"
        ) {
          fetchOrders();
        }
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [orders]);

  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
    }
  }

  console.log(orders);

  const handleCancelOrder = async (order) => {

    const res = await put(`orders/${order.id}/cancel`);

    if (!res.ok) {
      const err = await res.json();
      message.error(err.message || "Hủy đơn hàng thất bại");
      return;
    }

    notification.success({
      message: "Hủy đơn hàng thành công",
      description: `Đơn hàng #${order.orderCode} đã được hủy.`
    });

    await fetchOrders();
  };

  const showConfirmCancel = (order) => {
    console.log("id" + order.id)
    Modal.confirm({
      title: "Xác nhận hủy đơn",
      content: "Bạn có chắc muốn hủy đơn hàng này không?",
      okText: "Hủy đơn",
      cancelText: "Không",
      okType: "danger",
      onOk() {
        handleCancelOrder(order);
      },
      onCancel() {
      },
    });
  };

  const tabs = [
    { key: "", label: "Tất cả" },
    { key: "PENDING", label: "Chờ xác nhận" },
    { key: "CONFIRMED", label: "Đã xác nhận" },
    { key: "DELIVERING", label: "Đang giao" },
    { key: "DELIVERED", label: "Đã giao" },
    // { key: "COMPLETED", label: "Hoàn thành" },
    { key: "CANCELLED", label: "Đã hủy" },
  ];


  const statusMap = {
    PENDING: {
      label: "CHỜ XÁC NHẬN",
      className: "pending",
      icon: <FaRegClock />,
    },
    CONFIRMED: {
      label: "ĐÃ XÁC NHẬN",
      className: "confirmed",
      icon: <FaCheckCircle />,
    },
    DELIVERING: {
      label: "ĐANG GIAO",
      className: "delivering",
      icon: <MdLocalShipping />,
    },
    DELIVERED: {
      label: "ĐÃ GIAO",
      className: "delivered",
      icon: <FaCheckCircle />,
    },
    COMPLETED: {
      label: "HOÀN THÀNH",
      className: "completed",
      icon: <FaCheckCircle />,
    },
    CANCELLED: {
      label: "ĐÃ HỦY",
      className: "cancelled",
      icon: <FaTimesCircle />,
    },
  };

  const renderActions = (order) => {
    switch (order.status) {
      case "PENDING":
        return (
          <>
            <button className="myorders__product__btn" onClick={() => showConfirmCancel(order)}>
              Hủy đơn
            </button>
            <Link
              to={`/don-hang/${order.id}`}
              className="myorders__product__btn"
            >
              Xem chi tiết
            </Link>
          </>
        );

      case "CONFIRMED":
        return (
          <>
            {/* <button className="myorders__product__btn">
              Hủy đơn
            </button> */}
            {/* <button className="myorders__product__btn">
              Liên hệ shop
            </button> */}
            <Link
              to={`/don-hang/${order.id}`}
              className="myorders__product__btn"
            >
              Xem chi tiết
            </Link>
          </>
        );

      case "DELIVERING":
        return (
          <>
            {/* <button className="myorders__product__btn">
              Liên hệ shop
            </button> */}
            <Link
              to={`/don-hang/${order.id}`}
              className="myorders__product__btn"
            >
              Xem chi tiết
            </Link>
          </>
        );

      // case "DELIVERED":
      //   return (
      //     <>
      //       {/* <button className="myorders__product__btn myorders__product__btn--primary">
      //         Đã nhận hàng
      //       </button> */}
      //       <Link
      //         to={`/don-hang/${order.id}`}
      //         className="myorders__product__btn"
      //       >
      //         Xem chi tiết
      //       </Link>
      //     </>
      //   );

      case "DELIVERED":
        return (
          <>
            <button className="myorders__product__btn myorders__product__btn--primary">
              Mua lại
            </button>
            <Link
              to={`/don-hang/${order.id}`}
              className="myorders__product__btn"
            >
              Xem chi tiết
            </Link>
          </>
        );

      case "CANCELLED":
        return (
          <>
            <button className="myorders__product__btn myorders__product__btn--primary">
              Mua lại
            </button>
            <Link
              to={`/don-hang/${order.id}`}
              className="myorders__product__btn"
            >
              Xem chi tiết
            </Link>
          </>
        );

      default:
        return (
          <Link
            to={`/don-hang/${order.id}`}
            className="myorders__product__btn"
          >
            Xem chi tiết
          </Link>
        );
    }
  };

  const handleOpenReview = (order, product) => {
    setSelectedOrderId(order.id);
    setSelectedProduct({
      id: product.productId,
      name: product.productName,
      image: product.image,
    });
    setReviewOpen(true);
  };

  return (
    <>
      <div className="myorders">
        <div className="myorders__top">
          <div className="myorders__tab__list">
            {tabs.map(tab => (
              <button
                key={tab.key}
                className={`myorders__tab__item ${status === tab.key ? "active" : ""}`}
                onClick={() => setStatus(tab.key)}
              >
                {tab.label}
              </button>
            ))}
          </div>

          <div className="myorders__search">
            <div className="myorders__search__input">
              <IoSearch />
              <input type="text" placeholder="Tìm kiếm theo mã đơn hàng" onKeyDown={handleSearch} />
            </div>
          </div>
        </div>

        <div className="myorders__content">
          {orders.length > 0 ? (
            orders.map(order => {
              const currentStatus = statusMap[order.status];

              return (
                <div className="myorders__box" key={order.id}>
                  <div className="myorders__box__header">
                    <div className="myorder__box__header__left">
                      <Link to={`/cua-hang/${order.shopId}`} className="myorders__box__header__shop">
                        Shop: {order.shopName}
                      </Link>
                      <div className="myorders__box__header__code">
                        Mã đơn: {order.orderCode}
                      </div>
                    </div>

                    <div className={`myorders__box__header__status ${currentStatus.className}`}>
                      {currentStatus.icon}
                      <p>{currentStatus.label}</p>
                    </div>
                  </div>

                  <div className="myorders__box__body">
                    {order.items.map(p => (
                      <div className="myorders__product" key={p.id}>
                        <img src={p.image} alt="" className="myorders__product__image" />

                        <div className="myorders__product__info">
                          <Link to={`/san-pham/${p.productId}`} className="myorders__product__name">
                            {p.productName}
                          </Link>
                          <div className="myorders__product__desc">
                            Phân loại: {p.color}, {p.size}
                          </div>
                          <div className="myorders__product__quantity">
                            x{p.quantity}
                          </div>
                        </div>
                        <div className="myorders__product__right">
                          <div className="myorders__product__price">
                            {formatPrice2(p.price)}
                          </div>

                          {order.status === "DELIVERED" && (
                            <button
                              className="myorders__product__btn--review "
                              onClick={() => handleOpenReview(order, p)}
                            >
                              Đánh giá
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="myorders__product__footer">
                    <div className="myorders__product__footer__price">
                      Tổng thanh toán: <span>{formatPrice2(order.totalPrice)}</span>
                    </div>
                    <div className="myorders__product__footer__actions">
                      {renderActions(order)}
                    </div>
                  </div>
                </div>
              );
            })
          ) : (
            <div className="myorders__empty">
              <img
                src="https://deo.shopeemobile.com/shopee/shopee-pcmall-live-sg/orderlist/4751043c866ed52f9661.png"
                alt="Không có đơn hàng"
                className="myorders__empty__image"
              />
              <div className="myorders__empty__text">Chưa có đơn hàng</div>
            </div>
          )}
        </div>

      </div>

      <ReviewModal
        open={reviewOpen}
        onCancel={() => {
          setReviewOpen(false);
          setSelectedOrderId(null);
          setSelectedProduct(null);
        }}
        orderId={selectedOrderId}
        product={selectedProduct}
        onSuccess={() => {
          fetchOrders();
          setReviewOpen(false);
          setSelectedOrderId(null);
          setSelectedProduct(null);
        }}
      />
    </>
  )
}

export default MyOrders;