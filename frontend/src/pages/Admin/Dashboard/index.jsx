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
  Spin,
  Empty
} from "antd";
import {
  DollarOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  ShopOutlined,
  WarningOutlined,
  ThunderboltOutlined
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
import { get } from "../../../utils/request";

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
  SHIPPING: "Đang vận chuyển",
  DELIVERED: "Hoàn thành",
  CANCELLED: "Đã hủy"
};

const ACTIVITY_TYPE_LABEL = {
  NEW_USER: "Người dùng mới",
  NEW_SHOP: "Shop mới",
  NEW_ORDER: "Đơn hàng mới",
  NEW_PRODUCT_PENDING: "Sản phẩm chờ duyệt",
  FLASH_SALE_REGISTER: "Đăng ký flash sale"
};

function DashboardAdmin() {
  const [loading, setLoading] = useState(true);
  const [dashboard, setDashboard] = useState(null);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const res = await get("admin/dashboard");
      // setDashboard({
      //   "overview": {
      //     "totalRevenue": 120000000,
      //     "totalOrders": 1520,
      //     "ordersToday": 25,
      //     "totalUsers": 8450,
      //     "newUsersThisMonth": 230,
      //     "activeShops": 320,
      //     "pendingShops": 12,
      //     "revenueGrowthPercent": 18
      //   },
      //   "alerts": {
      //     "pendingProducts": 14,
      //     "pendingShops": 12,
      //     "outOfStockProducts": 35,
      //     "cancelledOrdersThisMonth": 21
      //   },
      //   "performance": {
      //     "deliveredRate": 68,
      //     "cancelledRate": 8,
      //     "confirmedRate": 16
      //   },
      //   "monthlyStats": [
      //     { "monthLabel": "Tháng 1", "orders": 120, "revenue": 50000000 },
      //     { "monthLabel": "Tháng 2", "orders": 135, "revenue": 55000000 }
      //   ],
      //   "orderStatusStats": [
      //     { "status": "PENDING", "count": 100 },
      //     { "status": "CONFIRMED", "count": 120 },
      //     { "status": "DELIVERED", "count": 800 }
      //   ],
      //   "topShops": [
      //     { "shopId": 1, "shopName": "Men Store A", "totalOrders": 120, "revenue": 32000000 }
      //   ],
      //   "recentActivities": [
      //     { "type": "NEW_USER", "title": "Nguyễn Văn A vừa đăng ký", "createdAt": "23/04/2026 14:30" }
      //   ]
      // });
      const data = await res.json();
      setDashboard(data);
    } catch (error) {
      console.error("Lỗi tải dashboard", error);
    } finally {
      setLoading(false);
    }
  };
  console.log(dashboard);

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

  const monthlyChart = useMemo(() => {
    return dashboard?.monthlyStats || [];
  }, [dashboard]);

  const topShops = dashboard?.topShops || [];
  const recentActivities = dashboard?.recentActivities || [];
  const alerts = dashboard?.alerts || {};

  const activityColumns = [
    {
      title: "Loại",
      dataIndex: "type",
      key: "type",
      render: (value) => (
        <Tag color="blue">{ACTIVITY_TYPE_LABEL[value] || value}</Tag>
      )
    },
    {
      title: "Nội dung",
      dataIndex: "title",
      key: "title"
    },
    {
      title: "Thời gian",
      dataIndex: "createdAt",
      key: "createdAt",
      width: 180
    }
  ];

  if (loading) {
    return (
      <div className="dashboard-admin dashboard-admin--loading">
        <Spin size="large" />
      </div>
    );
  }

  if (!dashboard) {
    return (
      <div className="dashboard-admin">
        <Empty description="Không tải được dữ liệu dashboard" />
      </div>
    );
  }

  return (
    <div className="dashboard-admin">
      <div className="dashboard-admin__header">
        <div>
          <h1 className="dashboard-admin__title">Tổng quan hệ thống</h1>
          <div className="dashboard-admin__subtitle">
            Theo dõi toàn bộ hoạt động của Menshop
          </div>
        </div>

        <div className="dashboard-admin__actions">
          <Button onClick={fetchDashboard}>Làm mới</Button>
          <Link to="/admin/products">
            <Button type="primary">Duyệt sản phẩm</Button>
          </Link>
        </div>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="dashboard-admin__stat-card">
            <Statistic
              title="Tổng doanh thu"
              value={dashboard.overview.totalRevenue}
              formatter={(value) => money(value)}
              prefix={<DollarOutlined />}
            />
            <div className="dashboard-admin__growth positive">
              {dashboard.overview.revenueGrowthPercent || 0}% so với tháng trước
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="dashboard-admin__stat-card">
            <Statistic
              title="Tổng đơn hàng"
              value={dashboard.overview.totalOrders}
              formatter={(value) => number(value)}
              prefix={<ShoppingCartOutlined />}
            />
            <div className="dashboard-admin__growth">
              {number(dashboard.overview.ordersToday)} đơn hôm nay
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="dashboard-admin__stat-card">
            <Statistic
              title="Tổng người dùng"
              value={dashboard.overview.totalUsers}
              formatter={(value) => number(value)}
              prefix={<UserOutlined />}
            />
            <div className="dashboard-admin__growth">
              {number(dashboard.overview.newUsersThisMonth)} mới trong tháng
            </div>
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="dashboard-admin__stat-card">
            <Statistic
              title="Shop hoạt động"
              value={dashboard.overview.activeShops}
              formatter={(value) => number(value)}
              prefix={<ShopOutlined />}
            />
            <div className="dashboard-admin__growth">
              {number(dashboard.overview.pendingShops)} shop chờ duyệt
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className="mt16">
        <Col xs={24} xl={16}>
          <Card
            title="Doanh thu & đơn hàng 6 tháng gần nhất"
            extra={<Tag color="processing">Realtime overview</Tag>}
            className="dashboard-admin__chart-card"
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
          <Card title="Đơn hàng theo trạng thái" className="dashboard-admin__chart-card">
            <ResponsiveContainer width="100%" height={340}>
              <PieChart>
                <Pie
                  data={orderStatusChart}
                  dataKey="value"
                  nameKey="name"
                  outerRadius={110}
                  label
                >
                  {orderStatusChart.map((entry, index) => {
                    const colors = ["#1677ff", "#52c41a", "#faad14", "#722ed1", "#ff4d4f"];
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
                <WarningOutlined /> Cảnh báo hệ thống
              </span>
            }
            className="dashboard-admin__alert-card"
          >
            <div className="dashboard-admin__alert-item danger">
              <span>Sản phẩm chờ duyệt</span>
              <strong>{number(alerts.pendingProducts)}</strong>
            </div>

            <div className="dashboard-admin__alert-item warning">
              <span>Shop chờ duyệt</span>
              <strong>{number(alerts.pendingShops)}</strong>
            </div>

            {/* <div className="dashboard-admin__alert-item orange">
              <span>Sản phẩm hết hàng</span>
              <strong>{number(alerts.outOfStockProducts)}</strong>
            </div>

            <div className="dashboard-admin__alert-item">
              <span>Đơn bị hủy trong tháng</span>
              <strong>{number(alerts.cancelledOrdersThisMonth)}</strong>
            </div> */}
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card
            title={
              <span>
                <ThunderboltOutlined /> Truy cập nhanh
              </span>
            }
            className="dashboard-admin__quick-card"
          >
            <div className="dashboard-admin__quick-grid">
              <Link to="/admin/banners" className="dashboard-admin__quick-btn">
                Quản lý banner
              </Link>
              <Link to="/admin/flash-sales" className="dashboard-admin__quick-btn">
                Flash sale
              </Link>
              <Link to="/admin/products" className="dashboard-admin__quick-btn">
                Duyệt sản phẩm
              </Link>
              <Link to="/admin/shops" className="dashboard-admin__quick-btn">
                Quản lý shop
              </Link>
              <Link to="/admin/orders" className="dashboard-admin__quick-btn">
                Quản lý đơn hàng
              </Link>
              <Link to="/admin/users" className="dashboard-admin__quick-btn">
                Quản lý người dùng
              </Link>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={8}>
          <Card title="Hiệu suất xử lý đơn" className="dashboard-admin__performance-card">
            <div className="dashboard-admin__performance-item">
              <div className="dashboard-admin__performance-label">
                Tỷ lệ hoàn thành đơn
              </div>
              <Progress percent={dashboard.performance.deliveredRate || 0} />
            </div>

            <div className="dashboard-admin__performance-item">
              <div className="dashboard-admin__performance-label">
                Tỷ lệ hủy đơn
              </div>
              <Progress percent={dashboard.performance.cancelledRate || 0} status="exception" />
            </div>

            <div className="dashboard-admin__performance-item">
              <div className="dashboard-admin__performance-label">
                Tỷ lệ xác nhận đơn
              </div>
              <Progress percent={dashboard.performance.confirmedRate || 0} />
            </div>
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} className="mt16">
        <Col xs={24} xl={10}>
          <Card title="Top shop doanh thu" className="dashboard-admin__shop-card">
            {topShops.length === 0 ? (
              <Empty description="Chưa có dữ liệu" />
            ) : (
              <>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={topShops}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="shopName" hide />
                    <YAxis />
                    <Tooltip formatter={(value) => money(value)} />
                    <Bar dataKey="revenue" fill="#1677ff" />
                  </BarChart>
                </ResponsiveContainer>

                <div className="dashboard-admin__shop-list">
                  {topShops.map((shop) => (
                    <div className="dashboard-admin__shop-item" key={shop.shopId}>
                      <div>
                        <div className="dashboard-admin__shop-name">{shop.shopName}</div>
                        <div className="dashboard-admin__shop-meta">
                          {number(shop.totalOrders)} đơn
                        </div>
                      </div>
                      <div className="dashboard-admin__shop-revenue">
                        {money(shop.revenue)}
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </Card>
        </Col>

        <Col xs={24} xl={14}>
          <Card title="Hoạt động gần đây" className="dashboard-admin__activity-card">
            <Table
              rowKey={(record, index) => `${record.type}-${index}`}
              columns={activityColumns}
              dataSource={recentActivities}
              pagination={false}
              scroll={{ x: 600 }}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default DashboardAdmin;