import "./OrderDetail.scss";
import { FaArrowLeft } from "react-icons/fa6";
import { FaCheck } from "react-icons/fa6";
import { TbRosetteDiscountCheckFilled } from "react-icons/tb";
import { MdLocalShipping, MdCancel } from "react-icons/md";
import { MdOutlinePendingActions } from "react-icons/md";
import { Link, useNavigate, useParams } from "react-router-dom";
import { FaUser } from "react-icons/fa";
import { IoLocationSharp } from "react-icons/io5";
import { IoMdWallet } from "react-icons/io";
import { IoShieldCheckmarkSharp } from "react-icons/io5";
import { SiAftership } from "react-icons/si";
import { BiSolidMessageDetail } from "react-icons/bi";
import { GoChecklist } from "react-icons/go";
import { Table } from "antd"
import { useEffect, useState } from "react";
import { get } from "../../../utils/request";
import { formatDateTime, formatDateTime2 } from "../../../utils/date";
import { formatPrice2 } from "../../../utils/price";

const STEP_CONFIG = [
  {
    key: "PENDING",
    label: "Chờ xác nhận",
    icon: <MdOutlinePendingActions />,
  },
  {
    key: "CONFIRMED",
    label: "Đã xác nhận",
    icon: <FaCheck />,
  },
  {
    key: "DELIVERING",
    label: "Đang giao",
    icon: <MdLocalShipping />,
  },
  {
    key: "DELIVERED",
    label: "Hoàn thành",
    icon: <TbRosetteDiscountCheckFilled />,
  },
];

const STEP_ORDER = {
  PENDING: 0,
  CONFIRMED: 1,
  DELIVERING: 2,
  DELIVERED: 3,
};

const getStepTime = (histories, status) => {
  const matched = histories?.filter((x) => x.status === status) || [];
  return matched.length ? matched[matched.length - 1].createdAt : null;
};

function OrderDetail() {
  const { id } = useParams();
  const [order, setOrder] = useState();
  const navigate = useNavigate();

  const fetchOrder = async () => {
    try {
      const res = await get(`shop/orders/${id}`);
      const data = await res.json();

      if (res.status === 400) {
        navigate('/404')
        return;
      }
      if (!res.ok) throw new Error(data.message || "Đã có lỗi xảy ra!");

      setOrder(data);
    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    fetchOrder();
  }, [id])

  console.log(order)

  const columns = [
    {
      title: 'Ảnh',
      key: 'imageUrl',
      render: ({ image }) => (
        <img src={image} className="image" />
      )
    },
    {
      title: 'Thông tin sản phẩm',
      key: 'info',
      render: (record) => (
        <>
          <div className="name">
            {record.productName}
          </div>
          <div className="sub">
            Phân loại: Size {record.size} - {record.color}
          </div>
        </>
      )
    },
    {
      title: 'Đơn giá',
      key: 'price',
      render: ({ price }) => (
        <>
          <span className="price">{formatPrice2(price)}</span>
        </>
      )
    },
    {
      title: 'Số lượng',
      dataIndex: "quantity",
      key: 'quantity'
    },
    {
      title: 'Thành tiền',
      key: 'total',
      render: ({ subtotal }) => (
        <>
          <span className="price">{formatPrice2(subtotal)}</span>
        </>
      )
    },
  ];

  const currentStepIndex = STEP_ORDER[order?.status] ?? -1;


  return (
    <>
      <div className="orderdetail-shop">
        <Link to="/shop/orders" className="orderdetail-shop__back">
          <FaArrowLeft />
          QUAY LẠI DANH SÁCH
        </Link>
        <div className="orderdetail-shop__title">
          Chi tiết đơn hàng #{order?.orderCode}
        </div>
        <div className="orderdetail-shop__subtitle">
          Ngày tạo: {formatDateTime(order?.createdAt)}
        </div>

        <div className="orderdetail-shop__stepper box">
          {STEP_CONFIG.map((step, index) => {
            const isActive = index <= currentStepIndex;
            const isCurrent = index === currentStepIndex;
            const time = getStepTime(order?.orderStatusHistories, step.key);

            return (
              <div
                key={step.key}
                className={`orderdetail-shop__step ${isActive ? "active" : ""} ${isCurrent ? "current" : ""}`}
              >
                <div className="icon">{step.icon}</div>
                <div className="label">{step.label}</div>
                {time && (<div className="time">{formatDateTime(time)}</div>)}
              </div>
            );
          })}
        </div>

        {order?.status === "CANCELLED" && (
          <div className="orderdetail-shop__cancel box">
            <div className="icon">
              <MdCancel />
            </div>
            <div className="content">
              <div className="label">Đơn hàng đã hủy</div>
              <div className="time">
                {formatDateTime(getStepTime(order?.orderStatusHistories, "CANCELLED"))}
              </div>
            </div>
          </div>
        )}

        <div className="orderdetail-shop__body">
          <div className="left">
            <div className="ship box">
              <div className="ship__title">
                <FaUser />
                Thông tin khách hàng
              </div>

              <div className="ship__row">
                <div className="ship__info">
                  <div className="ship__info__title">
                    HỌ VÀ TÊN
                  </div>
                  <div className="ship__info__content">
                    {order?.receiverName}
                  </div>

                  <div className="ship__info__title">
                    SỐ ĐIỆN THOẠI
                  </div>
                  <div className="ship__info__content">
                    {order?.receiverPhone}
                  </div>

                  {/* <div className="ship__info__title">
                    EMAIL
                  </div>
                  <div className="ship__info__content">
                    nguyenmanhkha3225@gmail.com
                  </div> */}
                </div>

                <div className="ship__address">
                  <div className="ship__info__title">
                    ĐỊA CHỈ GIAO HÀNG
                  </div>
                  <div className="ship__address__content">
                    <IoLocationSharp />
                    <span>{order?.address}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="products box">
              <div className="products__title">
                <GoChecklist />
                Sản phẩm ({order?.orderDetails.length})
              </div>

              <Table
                rowKey="id"
                dataSource={order?.orderDetails}
                columns={columns}
                pagination={false}
              />
            </div>
          </div>

          <div className="right">
            <div className="payment box">
              <div className="payment__title">
                <IoMdWallet />
                Thanh toán
              </div>
              <div className="payment__content">
                <div className="payment__row">
                  <div className="payment__name">
                    Tổng tiền hàng
                  </div>
                  <div className="payment__value">
                    {formatPrice2(order?.totalPrice)}
                  </div>
                </div>

                <div className="payment__row">
                  <div className="payment__name">
                    Phí vận chuyển
                  </div>
                  <div className="payment__value">
                    {formatPrice2(order?.shippingFee)}
                  </div>
                </div>

                {/* <div className="payment__row">
                  <div className="payment__name">
                    Giảm giá voucher
                  </div>
                  <div className="payment__value">
                    -50,000đ
                  </div>
                </div> */}
              </div>

              <div className="payment__total">
                <div className="payment__total__title">
                  Tổng thanh toán
                </div>

                <div className="payment__total__value">
                  {formatPrice2(order?.finalPrice)}
                </div>
              </div>

              {order?.paymentMethod === "VNPAY" ? (
                <div className="payment__method">
                  <div className="div">
                    <IoShieldCheckmarkSharp style={{ color: "green" }} />
                    Thanh toán qua VNPAY
                  </div>
                  {order?.paymentStatus === "PAID" && (
                    <div className="payment__method__time">
                      Thanh toán lúc:
                      {formatDateTime2(order?.paidAt)}
                    </div>
                  )}
                </div>
              ) : (
                <div className="payment__method">
                  <div>
                    <SiAftership style={{ color: "green" }} />
                    Thanh toán khi nhận hàng
                  </div>
                  {order?.paymentStatus === "PAID" && (
                    <div className="payment__method__time">
                      Thanh toán lúc:
                      {formatDateTime2(order?.paidAt)}
                    </div>
                  )}
                </div>
              )}
            </div>

            <div className="note box">
              <div className="note__title">
                <BiSolidMessageDetail />
                Ghi chú từ khách hàng
              </div>

              <div className="note__content">
                "{order?.note}"
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default OrderDetail;