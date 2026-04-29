import "./OrderDetail.scss";
import { FaLocationDot } from "react-icons/fa6";
import { GiCardboardBoxClosed } from "react-icons/gi";
import { IoTimerOutline } from "react-icons/io5";
import { FaCircleCheck } from "react-icons/fa6";
import { MdLocalShipping } from "react-icons/md";
import { MdCancel } from "react-icons/md";
import { TbRosetteDiscountCheckFilled } from "react-icons/tb";
import { MdShoppingCart } from "react-icons/md";
import { SiPayhip } from "react-icons/si";
import { Col, message, Modal, notification, Row } from 'antd';
import { Link, useNavigate, useParams } from "react-router-dom";
import { FaHandHoldingHand } from "react-icons/fa6";
import { get, put } from "../../../utils/request";
import { useEffect, useState } from "react";
import { formatPrice2 } from "../../../utils/price"
import { formatDateTime2 } from "../../../utils/date"
import { connectSocket, disconnectSocket, subscribeSocket } from "../../../utils/socket";

function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState({});
  const navigate = useNavigate();

  const fetchOrderDetail = async () => {
    const res = await get(`orders/${id}`);
    if (res.status === 400) {
      navigate("/403");
      return;
    }

    if (res.status === 404) {
      navigate("/404");
      return;
    }

    const data = await res.json();
    setOrder(data);
  };

  useEffect(() => {
    fetchOrderDetail();
  }, [id]);

  useEffect(() => {
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket(`/topic/orders/${id}`, (data) => {
        if (!data) return;

        if (
          data.type === "ORDER_UPDATED_STATUS" &&
          Number(data.orderId) === Number(id)
        ) {
          fetchOrderDetail();
        }
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [id, order]);

  const getOrderStatusText = (status) => {
    switch (status) {
      case "PENDING":
        return "CHỜ XÁC NHẬN";
      case "CONFIRMED":
        return "ĐÃ XÁC NHẬN";
      case "DELIVERING":
        return "ĐANG GIAO HÀNG";
      case "DELIVERED":
        return "ĐÃ GIAO";
      case "CANCELLED":
        return "ĐÃ HỦY";
      default:
        return status;
    }
  };

  const getStatusHistoryIcon = (status) => {
    switch (status) {
      case "PENDING":
        return <MdShoppingCart />;
      case "CONFIRMED":
        return <TbRosetteDiscountCheckFilled />;
      case "DELIVERING":
        return <MdLocalShipping />;
      case "DELIVERED":
        return <FaCircleCheck />;
      case "CANCELLED":
        return <MdCancel />;
      default:
        return status;
    }
  };


  const handleCancelOrder = async () => {
    const res = await put(`orders/${id}/cancel`);

    if (!res.ok) {
      const err = await res.json();
      message.error(err.message || "Hủy đơn hàng thất bại");
      return;
    }

    notification.success({
      message: "Hủy đơn hàng thành công",
      description: `Đơn hàng #${order.orderCode} đã được hủy.`
    });

    await fetchOrderDetail();
  };

  const showConfirmCancel = () => {
    Modal.confirm({
      title: "Xác nhận hủy đơn",
      content: "Bạn có chắc muốn hủy đơn hàng này không?",
      okText: "Hủy đơn",
      cancelText: "Không",
      okType: "danger",
      onOk() {
        handleCancelOrder();
      },
      onCancel() {
      },
    });
  };

  console.log(order);

  return (
    <>
      <div className="orderdetail">
        <div className="container">
          <div className="orderdetail__header">
            <div className="orderdetail__header__info">
              <h1 className="orderdetail__header__title">
                Đơn hàng #{order?.orderCode}
              </h1>
              <div className="orderdetail__header__status">
                {getOrderStatusText(order.status)}
              </div>
            </div>

            <div className="orderdetail__header__date">
              <span>Ngày đặt:</span> {formatDateTime2(order.createdAt)}
            </div>
          </div>

          <Row gutter={30} className="orderdetail__body">
            <Col span={16} className="orderdetail__body__left">
              <Row gutter={[20, 20]} className="orderdetail__body__left__top">
                <Col span={12}>
                  <div className="orderdetail__card orderdetail__shipping-info">
                    <div className="orderdetail__card__top">
                      <FaLocationDot />
                      <span>Thông tin nhận hàng</span>
                    </div>
                    <div className="orderdetail__card__name">
                      {order?.receiverName}
                    </div>
                    <div className="orderdetail__card__desc">
                      {order?.receiverPhone}
                    </div>
                    <div className="orderdetail__card__desc">
                      {order?.address}
                    </div>
                  </div>
                </Col>
                <Col span={12} >
                  <div className="orderdetail__card orderdetail__shipping-method">
                    <div className="orderdetail__card__top">
                      <GiCardboardBoxClosed />
                      <span>Phương thức vận chuyển</span>
                    </div>
                    <div className="orderdetail__card__name">
                      {order?.shippingFee === 15000 ? "Giao hàng tiết kiệm" : "Giao hàng nhanh"}
                    </div>
                    <div className="orderdetail__card__desc">
                      Phí vận chuyển: {formatPrice2(order?.shippingFee)}
                    </div>
                  </div>
                </Col>

                <Col span={24} >
                  <div className="orderdetail__card orderdetail__status">
                    <div className="orderdetail__card__top">
                      <IoTimerOutline />
                      <span>Trạng thái đơn hàng</span>
                    </div>

                    {[...(order.orderStatusHistories || [])].reverse().map((it, idx) => (
                      <div className={`orderdetail__status__item ${idx === 0 && "active"}`} key={idx}>
                        <div className="orderdetail__status__icon">
                          {getStatusHistoryIcon(it.status)}
                        </div>
                        <div className="orderdetail__status__detail">
                          <div className="orderdetail__status__title">
                            {it.title}
                          </div>
                          <div className="orderdetail__status__desc">
                            {formatDateTime2(it.createdAt)} - {it.description}
                          </div>
                        </div>
                      </div>
                    ))}

                  </div>
                </Col>
              </Row>

            </Col>

            <Col span={8} >
              <div className="orderdetail__body__right">
                <div className="orderdetail__card  product__list">
                  <div className="product__list__title">
                    Sản phẩm ({order?.orderDetails?.length || 0})
                  </div>

                  {order?.orderDetails?.map(p => (
                    <Link to={`/san-pham/${p.id}`} key={p.id}>
                      <div className="product">
                        <img src={p.image} alt="" className="product__image" />

                        <div className="product__info">
                          <div className="product__name">
                            {p.productName}
                          </div>
                          <div className="product__desc">
                            Màu: {p.color} | Size: {p.size} | SL: {p.quantity}
                          </div>
                          <div className="product__price">
                            {formatPrice2(p.subtotal)}
                          </div>
                        </div>
                      </div>
                    </Link>
                  ))}


                  <div className="product__list__row">
                    <div>Tạm tính</div>
                    <div>{formatPrice2(order?.totalPrice)}</div>
                  </div>

                  <div className="product__list__row">
                    <div>Phí vận chuyển</div>
                    <div>{formatPrice2(order?.shippingFee)}</div>
                  </div>

                  {/* <div className="product__list__row">
                    <div>Giảm giá voucher</div>
                    <div style={{color: "red"}}>-50.000đ</div>
                  </div> */}

                  <div className="product__list__total">
                    <h2 className="product__list__total__title">
                      Tổng cộng
                    </h2>
                    <h2 className="product__list__total__price">
                      {formatPrice2(order?.finalPrice)}
                    </h2>
                  </div>
                </div>

                <div className="orderdetail__card note">
                  <div className="orderdetail__card__top">
                    Ghi chú cho người bán
                  </div>
                  <div className="note__content">
                    "{order?.note}"
                  </div>
                </div>

                <div className="orderdetail__card  payment">
                  <div className="orderdetail__card__top">
                    Thanh toán
                  </div>

                  <div className="payment__content">

                    {order?.paymentMethod === "VNPAY" ? (
                      <>
                        <div className="payment__icon">
                          <SiPayhip />
                        </div>
                        <div className="payment__info">
                          <div className="payment__name">
                            Thanh toán qua VnPay
                          </div>
                          <div className="payment__desc">
                            {order.paymentStatus === "PAID"
                              ? `Đã thanh toán: ${formatDateTime2(order.paidAt)}`
                              : "Chưa thanh toán"}
                          </div>
                        </div>
                      </>
                    ) : (
                      <>
                        <div className="payment__icon">
                          <FaHandHoldingHand />
                        </div>
                        <div className="payment__info">
                          <div className="payment__name">
                            Thanh toán khi nhận hàng
                          </div>
                        </div>
                      </>
                    )}
                  </div>
                </div>

                {/* {order?.status === "DELIVERING" && (
                  <button className="received">
                    <FaCircleCheck />
                    Đã nhận hàng
                  </button>
                )} */}

                {/* <button className="contact">
                  Liên hệ người bán
                </button> */}

                {order?.status === "PENDING" && (
                  <button className="cancel" onClick={showConfirmCancel}>
                    Hủy đơn hàng
                  </button>
                )}

              </div>
            </Col>
          </Row>
        </div>
      </div>
    </>
  )
}

export default OrderDetail;