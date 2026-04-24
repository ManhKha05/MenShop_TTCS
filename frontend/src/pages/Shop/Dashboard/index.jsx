import "./Dashboard.scss";
import {
  Card,
  Col,
  Row,
  Statistic,
  Table,
  Tag,
  Progress,
  Button,
  Empty,
  Spin,
  Rate
} from "antd";
import {
  DollarOutlined,
  ShoppingCartOutlined,
  AppstoreOutlined,
  StarOutlined,
  ThunderboltOutlined,
  WarningOutlined
} from "@ant-design/icons";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar
} from "recharts";
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {get} from "../../../utils/request";

const money = (value) =>
  new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND"
  }).format(value || 0);

const number = (value) =>
  new Intl.NumberFormat("vi-VN").format(value || 0);

const ORDER_STATUS_LABEL = {
  PENDING: "Chờ xác nhận",
  CONFIRMED: "Đã xác nhận",
  SHIPPING: "Đang giao",
  DELIVERED: "Hoàn thành",
  CANCELLED: "Đã hủy"
};

const ORDER_STATUS_COLOR = {
  PENDING: "gold",
  CONFIRMED: "blue",
  SHIPPING: "processing",
  DELIVERED: "success",
  CANCELLED: "error"
};

function ShopDashboard() {
  const [loading, setLoading] = useState(true);
  const [dashboard, setDashboard] = useState(null);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const res = await get("shop/dashboard");
      const data = await res.json();
      setDashboard(data);
    } catch (error) {
      console.error("Lỗi tải dashboard shop", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const orderStatusChart = useMemo(() => {
    if (!dashboard?.orderStatusStats) return [];
    return dashboard.orderStatusStats.map((item) => ({
      name: ORDER_STATUS_LABEL[item.status] || item.status,
      value: item.count
    }));
  }, [dashboard]);

  const monthlyChart = dashboard?.monthlyStats || [];
  const topProducts = dashboard?.topProducts || [];
  const recentOrders = dashboard?.recentOrders || [];
  const alerts = dashboard?.alerts || {};
  const overview = dashboard?.overview || {};
  const performance = dashboard?.performance || {};

  const recentOrderColumns = [
    {
      title: "Mã đơn",
      dataIndex: "orderCode",
      key: "orderCode",
      render: (value, record) => (
        <Link to={`/shop/orders/${record.id}`} className="shop-dashboard__table-link">
          #{value}
        </Link>
      )
    },
    {
      title: "Khách hàng",
      dataIndex: "customerName",
      key: "customerName"
    },
    {
      title: "Tổng tiền",
      dataIndex: "finalTotal",
      key: "finalTotal",
      render: (value) => money(value)
    },
    {
      title: "Trạng thái",
      dataIndex: "status",
      key: "status",
      render: (value) => (
        <Tag color={ORDER_STATUS_COLOR[value] || "default"}>
          {ORDER_STATUS_LABEL[value] || value}
        </Tag>
      )
    },
    {
      title: "Thời gian",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 170
    }
  ];

  if (loading) {
    return (
      <div className="shop-dashboard shop-dashboard--loading">
        <Spin size="large" />
      </div>
    );
  }

  console.log("dashboard" , dashboard);

  if (!dashboard) {
    return (
      <div className="shop-dashboard">
        <Empty description="Không tải được dữ liệu dashboard" />
      </div>
    );
  }

  return (
    <div className="shop-dashboard">
      <div className="shop-dashboard__header">
        <div>
          <h1 className="shop-dashboard__title">Tổng quan cửa hàng</h1>
          <div className="shop-dashboard__subtitle">
            Theo dõi hiệu suất kinh doanh của shop
          </div>
        </div>

        <div className="shop-dashboard__header-actions">
          <Button onClick={fetchDashboard}>Làm mới</Button>
          <Link to="/shop/edit-product">
            <Button type="primary">Thêm sản phẩm</Button>
          </Link>
        </div>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} xl={6}>
          <Card className="shop-dashboard__stat-card">
            <Statistic
              title="Doanh thu tháng này"
              value={overview.revenueThisMonth}
              formatter={(value) => money(value)}
              prefix={<DollarOutlined />}
            />
            <div className="shop-dashboard__stat-sub positive">
              +{overview.revenueGrowthPercent || 0}% so với tháng trước
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} xl={6}>
          <Card className="shop-dashboard__stat-card">
            <Statistic
              title="Đơn hàng tháng này"
              value={overview.ordersThisMonth}
              formatter={(value) => number(value)}
              prefix={<ShoppingCartOutlined />}
            />
            <div className="shop-dashboard__stat-sub">
              {number(overview.pendingOrders)} đơn chờ xác nhận
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} xl={6}>
          <Card className="shop-dashboard__stat-card">
            <Statistic
              title="Sản phẩm đang bán"
              value={overview.activeProducts}
              formatter={(value) => number(value)}
              prefix={<AppstoreOutlined />}
            />
            <div className="shop-dashboard__stat-sub">
              {number(overview.outOfStockProducts)} sản phẩm hết hàng
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} xl={6}>
          <Card className="shop-dashboard__stat-card">
            <Statistic
              title="Đánh giá trung bình"
              value={overview.averageRating || 0}
              precision={1}
              prefix={<StarOutlined />}
            />
            <div className="shop-dashboard__rating-wrap">
              <Rate disabled allowHalf value={overview.averageRating || 0} />
              <span className="shop-dashboard__rating-count">
                ({number(overview.totalReviews)} đánh giá)
              </span>
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className="mt16">
        <Col xs={24} xl={16}>
          <Card
            title="Doanh thu và đơn hàng 6 tháng gần nhất"
            extra={<Tag color="processing">Cập nhật từ shop</Tag>}
            className="shop-dashboard__chart-card"
          >
            <ResponsiveContainer width="100%" height={340}>
              <LineChart data={monthlyChart}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="monthLabel" />
                <YAxis yAxisId="left" />
                <YAxis yAxisId="right" orientation="right" />
                <Tooltip
                  formatter={(value, name) =>
                    name === "revenue"
                      ? [money(value), "Doanh thu"]
                      : [number(value), "Đơn hàng"]
                  }
                />
                <Line
                  yAxisId="left"
                  type="monotone"
                  dataKey="revenue"
                  name="revenue"
                  stroke="#1677ff"
                  strokeWidth={3}
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="orders"
                  name="orders"
                  stroke="#fa541c"
                  strokeWidth={3}
                />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>

        <Col xs={24} xl={8}>
          <Card title="Đơn hàng theo trạng thái" className="shop-dashboard__chart-card">
            <ResponsiveContainer width="100%" height={340}>
              <PieChart>
                <Pie
                  data={orderStatusChart}
                  dataKey="value"
                  nameKey="name"
                  outerRadius={110}
                  label
                >
                  {orderStatusChart.map((item, index) => {
                    const colors = ["#faad14", "#1677ff", "#722ed1", "#52c41a", "#ff4d4f"];
                    return <Cell key={index} fill={colors[index % colors.length]} />;
                  })}
                </Pie>
                <Tooltip formatter={(value) => number(value)} />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className="mt16">
        <Col xs={24} lg={8}>
          <Card
            title={
              <span>
                <WarningOutlined /> Cảnh báo cần xử lý
              </span>
            }
            className="shop-dashboard__alert-card"
          >
            <div className="shop-dashboard__alert-item danger">
              <span>Đơn chờ xác nhận</span>
              <strong>{number(alerts.pendingOrders)}</strong>
            </div>

            <div className="shop-dashboard__alert-item orange">
              <span>Sản phẩm sắp hết hàng</span>
              <strong>{number(alerts.lowStockProducts)}</strong>
            </div>

            <div className="shop-dashboard__alert-item warning">
              <span>Sản phẩm hết hàng</span>
              <strong>{number(alerts.outOfStockProducts)}</strong>
            </div>

            <div className="shop-dashboard__alert-item">
              <span>Đánh giá 1-2 sao</span>
              <strong>{number(alerts.lowRatingReviews)}</strong>
            </div>

            <div className="shop-dashboard__alert-item">
              <span>Sản phẩm chờ duyệt</span>
              <strong>{number(alerts.pendingProducts)}</strong>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card
            title={
              <span>
                <ThunderboltOutlined /> Truy cập nhanh
              </span>
            }
            className="shop-dashboard__quick-card"
          >
            <div className="shop-dashboard__quick-grid">
              <Link to="/shop/edit-product" className="shop-dashboard__quick-btn">
                Thêm sản phẩm
              </Link>
              <Link to="/shop/orders" className="shop-dashboard__quick-btn">
                Xử lý đơn
              </Link>
              <Link to="/shop/flash-sale" className="shop-dashboard__quick-btn">
                Flash sale
              </Link>
              <Link to="/shop/products" className="shop-dashboard__quick-btn">
                Quản lý sản phẩm
              </Link>
              <Link to="/shop/reviews" className="shop-dashboard__quick-btn">
                Xem đánh giá
              </Link>
              <Link to="/shop/profile" className="shop-dashboard__quick-btn">
                Thông tin shop
              </Link>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="Hiệu suất vận hành" className="shop-dashboard__performance-card">
            <div className="shop-dashboard__performance-item">
              <div className="shop-dashboard__performance-label">
                Tỷ lệ hoàn thành đơn
              </div>
              <Progress percent={performance.deliveredRate || 0} />
            </div>

            <div className="shop-dashboard__performance-item">
              <div className="shop-dashboard__performance-label">
                Tỷ lệ hủy đơn
              </div>
              <Progress percent={performance.cancelledRate || 0} status="exception" />
            </div>

            <div className="shop-dashboard__performance-item">
              <div className="shop-dashboard__performance-label">
                Tỷ lệ xác nhận đơn
              </div>
              <Progress percent={performance.confirmedRate || 0} />
            </div>

            <div className="shop-dashboard__performance-item">
              <div className="shop-dashboard__performance-label">
                Tỷ lệ giao thành công
              </div>
              <Progress percent={performance.shippingSuccessRate || 0} />
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className="mt16">
        <Col xs={24} xl={10}>
          <Card title="Top sản phẩm bán chạy" className="shop-dashboard__product-card">
            {topProducts.length === 0 ? (
              <Empty description="Chưa có dữ liệu" />
            ) : (
              <>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={topProducts}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="productName" hide />
                    <YAxis />
                    <Tooltip formatter={(value) => number(value)} />
                    <Bar dataKey="soldCount" fill="#1677ff" />
                  </BarChart>
                </ResponsiveContainer>

                <div className="shop-dashboard__product-list">
                  {topProducts.map((item) => (
                    <div className="shop-dashboard__product-item" key={item.productId}>
                      <div className="shop-dashboard__product-left">
                        <img
                          src={item.image || "https://via.placeholder.com/56"}
                          alt={item.productName}
                          className="shop-dashboard__product-image"
                        />
                        <div>
                          <div className="shop-dashboard__product-name">
                            {item.productName}
                          </div>
                          <div className="shop-dashboard__product-meta">
                            Đã bán {number(item.soldCount)} | Tồn kho {number(item.stock)}
                          </div>
                        </div>
                      </div>
                      <div className="shop-dashboard__product-revenue">
                        {money(item.revenue)}
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </Card>
        </Col>

        <Col xs={24} xl={14}>
          <Card title="Đơn hàng gần đây" className="shop-dashboard__order-card">
            <Table
              rowKey="id"
              columns={recentOrderColumns}
              dataSource={recentOrders}
              pagination={false}
              scroll={{ x: 700 }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default ShopDashboard;