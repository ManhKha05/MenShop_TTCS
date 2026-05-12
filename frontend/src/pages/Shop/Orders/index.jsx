import "./Orders.scss";
import { MdFeaturedPlayList } from "react-icons/md";
import { MdOutlinePendingActions } from "react-icons/md";
import { MdLocalShipping } from "react-icons/md";
import { IoMdCheckmarkCircleOutline } from "react-icons/io";
import { MdCancel } from "react-icons/md";
import { IoMdSearch } from "react-icons/io";
import { IoFilterSharp } from "react-icons/io5";
import { FaCalendarCheck } from "react-icons/fa6";
import { Button, Form, Input, Select, DatePicker, Spin } from "antd";
import OrderTable from "../../../components/OrderTable";
import { useCallback, useEffect, useState } from "react";
import { get } from "../../../utils/request";
import dayjs from "dayjs";
import { connectSocket, subscribeSocket, unsubscribe } from "../../../utils/socket";


const { RangePicker } = DatePicker;
const statCards = [
  {
    key: "total",
    title: "Tổng đơn hàng",
    icon: <MdFeaturedPlayList />,
    style: { color: "#0052CC", backgroundColor: "#E9F0FF" }
  },
  {
    key: "pending",
    title: "Chờ xác nhận",
    icon: <MdOutlinePendingActions />,
    style: { color: "#91472C", backgroundColor: "#F6EDEB" }
  },
  {
    key: "confirmed",
    title: "Đã xác nhận",
    icon: <FaCalendarCheck />,
    style: { color: "#7AC24B", backgroundColor: "#EEF8E8" }
  },
  {
    key: "shipping",
    title: "Đang giao",
    icon: <MdLocalShipping />,
    style: { color: "#2E5BFF", backgroundColor: "#E2ECFF" }
  },
  {
    key: "delivered",
    title: "Hoàn thành",
    icon: <IoMdCheckmarkCircleOutline />,
    style: { color: "#1DB45A", backgroundColor: "#E3F9EB" }
  },
  {
    key: "cancelled",
    title: "Đã hủy",
    icon: <MdCancel />,
    style: { color: "#C51111", backgroundColor: "#FFF0F0" }
  }
];

function Orders() {
  const shopId = localStorage.getItem("shopId");
  const [stats, setStats] = useState({
    total: 0,
    pending: 0,
    confirmed: 0,
    shipping: 0,
    delivered: 0,
    cancelled: 0
  });
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(0);
  const [filters, setFilters] = useState({
    code: "",
    status: "",
    shippingStatus: "",
    paymentMethod: "",
    fromDate: null,
    toDate: null,
  });
  const [loading, setLoading] = useState(false);

  const fetchStats = useCallback(async () => {
    try {
      const res = await get("shop/orders/stats");
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || "Lấy thống kê thất bại");
      setStats(data);
    } catch (error) {
      console.error(error);
    }
  }, []);

  const fetchOrders = useCallback(async () => {
    try {
      const res = await get(`shop/orders`, {
        page: page - 1,
        size: pageSize,
        ...filters
      });

      const data = await res.json();
      if (!res.ok) throw new Error(data.message || "Lấy danh sách đơn hàng thất bại");

      setOrders(data.content || []);
      setTotal(data.totalElements || 0);
    } catch (error) {
      console.error(error);
    }
  }, [page, pageSize, filters]);

  useEffect(() => {
    fetchStats();
    fetchOrders();
  }, [fetchStats, fetchOrders]);

  useEffect(() => {
    if (!shopId) return;

    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket(`/topic/shop-orders/${shopId}`, (data) => {
        console.log("Shop order event:", data);

        if (!data) return;

        if (
          data.type === "ORDER_CREATED" ||
          data.type === "ORDER_CANCELLED" ||
          data.type === "ORDER_UPDATED_STATUS"
        ) {
          setPage(1); // order mới nên quay về trang đầu
          fetchStats();
          fetchOrders();
        }
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [shopId, fetchStats, fetchOrders]);

  console.log(orders);

  const handleFilter = (values) => {
    setPage(1);

    setFilters({
      code: values.code || "",
      status: values.status || "",
      shippingStatus: values.shippingStatus || "",
      paymentMethod: values.paymentMethod || "",
      fromDate: values.time?.[0]
        ? dayjs(values.time[0]).startOf("day").format("YYYY-MM-DDTHH:mm:ss")
        : null,
      toDate: values.time?.[1]
        ? dayjs(values.time[1]).endOf("day").format("YYYY-MM-DDTHH:mm:ss")
        : null,
    });
  }

  // if (loading) {
  //   return (
  //     <div className="dashboard-admin dashboard-admin--loading">
  //       <Spin size="large" />
  //     </div>
  //   );
  // }

  return (
    <>
      <div className="orders-shop">
        <div className="orders-shop__stats">
          {statCards.map((item) => (
            <div className="orders-shop__stat" key={item.key}>
              <div className="orders-shop__stat__icon" style={item.style}>
                {item.icon}
              </div>
              <div className="orders-shop__stat__title">{item.title}</div>
              <div className="orders-shop__stat__value">
                {stats[item.key]?.toLocaleString("vi-VN") || 0}
              </div>
            </div>
          ))}
        </div>

        <div className="orders-shop__filter">
          <Form
            // form={form}
            layout="vertical"
            className="orders-shop__filter__form"
            onFinish={handleFilter}
          >
            <Form.Item
              label="Đơn hàng"
              name="code"
            >
              <Input placeholder="Mã đơn" prefix={<IoMdSearch />} />
            </Form.Item>

            <Form.Item
              label="Trạng thái đơn"
              name="status"
              initialValue=""
            >
              <Select
                style={{ width: 170 }}
                options={[
                  { value: "", label: "Tất cả trạng thái" },
                  { value: "PENDING", label: "Chờ xác nhận" },
                  { value: "CONFIRMED", label: "Đã xác nhận" },
                  { value: "DELIVERED", label: "Đã giao" },
                  { value: "CANCELLED", label: "Đã hủy" },
                ]}
              />
            </Form.Item>

            {/* <Form.Item
              label="Vận chuyển"
              name="shippingStatus"
              initialValue=""
            >
              <Select
                style={{ width: 190 }}
                options={[
                  { value: "", label: "Tất cả vận chuyển" },
                  { value: "READY_TO_PICK", label: "Chờ lấy hàng" },
                  { value: "PICKING", label: "Đang lấy hàng" },
                  { value: "PICKED", label: "Đã lấy hàng" },
                  { value: "STORING", label: "Đang lưu kho" },
                  { value: "SORTING", label: "Đang phân loại" },
                  { value: "TRANSPORTING", label: "Đang vận chuyển" },
                  { value: "DELIVERING", label: "Đang giao" },
                  { value: "DELIVERED", label: "Đã giao" },
                  { value: "DELIVERY_FAIL", label: "Giao thất bại" },
                  { value: "RETURN", label: "Đang hoàn hàng" },
                  { value: "RETURNED", label: "Đã hoàn hàng" },
                  { value: "CANCEL", label: "Đã hủy vận đơn" },
                ]}
              />
            </Form.Item> */}

            <Form.Item
              label="Khoảng thời gian"
              name="time"
            >
              <RangePicker />
            </Form.Item>

            <Form.Item
              label="Thanh toán"
              name="paymentMethod"
              initialValue={""}
            >
              <Select
                style={{ width: 170 }}
                options={[
                  { value: '', label: 'Mọi phương thức' },
                  { value: 'COD', label: 'COD' },
                  { value: 'VNPAY', label: 'VNPAY' },
                ]}
              />
            </Form.Item>

            <Form.Item label="">
              <Button type="primary" htmlType="submit" icon={<IoFilterSharp />}>
                Lọc đơn
              </Button>
            </Form.Item>
          </Form>
        </div>

        <div className="orders-shop__table">
          <OrderTable
            orders={orders}
            page={page}
            pageSize={pageSize}
            total={total}
            onChangePage={(newPage, newPageSize) => {
              setPage(newPage);
              setPageSize(newPageSize);
            }}
            onReload={() => {
              fetchOrders();
              fetchStats();
            }}
          />
        </div>
      </div>
    </>
  )
}

export default Orders;