import "./Revenue.scss";
import {
  Card,
  Row,
  Col,
  Statistic,
  DatePicker,
  Button,
  Table,
  Empty,
  Spin,
  Tag
} from "antd";
import {
  DollarOutlined,
  ShoppingCartOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  BarChartOutlined
} from "@ant-design/icons";
import {
  ResponsiveContainer,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from "recharts";
import { useEffect, useMemo, useState } from "react";
import dayjs from "dayjs";
import { get } from "../../../utils/request";

const { RangePicker } = DatePicker;

const money = (value) =>
  new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND"
  }).format(value || 0);

const number = (value) =>
  new Intl.NumberFormat("vi-VN").format(value || 0);

function ShopRevenue() {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);

  const [filter, setFilter] = useState({
    fromDate: dayjs().startOf("month"),
    toDate: dayjs().endOf("day"),
    preset: "month"
  });

  const fetchData = async () => {
    try {
      setLoading(true);

      const res = await get("shop/statistics/revenue", {
        fromDate: filter.fromDate.format("YYYY-MM-DD"),
        toDate: filter.toDate.format("YYYY-MM-DD")
      });

      const data = await res.json();

      setData(data);
    } catch (error) {
      console.error("Lỗi tải thống kê doanh thu", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const applyPreset = (preset) => {
    if (preset === "today") {
      setFilter({
        preset,
        fromDate: dayjs().startOf("day"),
        toDate: dayjs().endOf("day")
      });
    } else if (preset === "7days") {
      setFilter({
        preset,
        fromDate: dayjs().subtract(6, "day").startOf("day"),
        toDate: dayjs().endOf("day")
      });
    } else if (preset === "30days") {
      setFilter({
        preset,
        fromDate: dayjs().subtract(29, "day").startOf("day"),
        toDate: dayjs().endOf("day")
      });
    } else if (preset === "month") {
      setFilter({
        preset,
        fromDate: dayjs().startOf("month"),
        toDate: dayjs().endOf("day")
      });
    }
  };

  const overview = data?.overview || {};
  const chartData = data?.chartData || [];
  const topProducts = data?.topProducts || [];
  const detailRows = data?.detailRows || [];

  const detailColumns = useMemo(
    () => [
      {
        title: "Ngày",
        dataIndex: "label",
        key: "label"
      },
      {
        title: "Tổng đơn",
        dataIndex: "totalOrders",
        key: "totalOrders",
        render: (value) => number(value)
      },
      {
        title: "Đơn hoàn thành",
        dataIndex: "deliveredOrders",
        key: "deliveredOrders",
        render: (value) => <Tag color="success">{number(value)}</Tag>
      },
      {
        title: "Đơn hủy",
        dataIndex: "cancelledOrders",
        key: "cancelledOrders",
        render: (value) => <Tag color="error">{number(value)}</Tag>
      },
      {
        title: "Doanh thu",
        dataIndex: "revenue",
        key: "revenue",
        render: (value) => money(value)
      },
      {
        title: "Tỷ lệ hủy",
        dataIndex: "cancelRate",
        key: "cancelRate",
        render: (value) => `${value || 0}%`
      }
    ],
    []
  );

  return (
    <div className="shop-revenue">
      <div className="shop-revenue__header">
        <div>
          <h1 className="shop-revenue__title">Thống kê doanh thu</h1>
          <div className="shop-revenue__subtitle">
            Theo dõi hiệu quả kinh doanh của cửa hàng
          </div>
        </div>

        <div className="shop-revenue__actions">
          <Button
            type={filter.preset === "today" ? "primary" : "default"}
            onClick={() => applyPreset("today")}
          >
            Hôm nay
          </Button>
          <Button
            type={filter.preset === "7days" ? "primary" : "default"}
            onClick={() => applyPreset("7days")}
          >
            7 ngày
          </Button>
          <Button
            type={filter.preset === "30days" ? "primary" : "default"}
            onClick={() => applyPreset("30days")}
          >
            30 ngày
          </Button>
          <Button
            type={filter.preset === "month" ? "primary" : "default"}
            onClick={() => applyPreset("month")}
          >
            Tháng này
          </Button>

          <RangePicker
            value={[filter.fromDate, filter.toDate]}
            onChange={(dates) => {
              if (!dates) return;
              setFilter({
                preset: "custom",
                fromDate: dates[0].startOf("day"),
                toDate: dates[1].endOf("day")
              });
            }}
            format="DD/MM/YYYY"
          />

          <Button type="primary" onClick={fetchData}>
            Xem thống kê
          </Button>
        </div>
      </div>

      {loading ? (
        <div className="shop-revenue__loading">
          <Spin size="large" />
        </div>
      ) : !data ? (
        <Empty description="Không có dữ liệu" />
      ) : (
        <>
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={12} xl={4}>
              <Card className="shop-revenue__card">
                <Statistic
                  title="Tổng doanh thu"
                  value={overview.totalRevenue}
                  formatter={(value) => money(value)}
                  prefix={<DollarOutlined />}
                />
              </Card>
            </Col>

            <Col xs={24} sm={12} xl={4}>
              <Card className="shop-revenue__card">
                <Statistic
                  title="Tổng đơn hàng"
                  value={overview.totalOrders}
                  formatter={(value) => number(value)}
                  prefix={<ShoppingCartOutlined />}
                />
              </Card>
            </Col>

            <Col xs={24} sm={12} xl={4}>
              <Card className="shop-revenue__card">
                <Statistic
                  title="Đơn hoàn thành"
                  value={overview.deliveredOrders}
                  formatter={(value) => number(value)}
                  prefix={<CheckCircleOutlined />}
                />
              </Card>
            </Col>

            <Col xs={24} sm={12} xl={4}>
              <Card className="shop-revenue__card">
                <Statistic
                  title="Đơn bị hủy"
                  value={overview.cancelledOrders}
                  formatter={(value) => number(value)}
                  prefix={<CloseCircleOutlined />}
                />
              </Card>
            </Col>

            <Col xs={24} sm={12} xl={4}>
              <Card className="shop-revenue__card">
                <Statistic
                  title="Giá trị đơn TB"
                  value={overview.averageOrderValue}
                  formatter={(value) => money(value)}
                  prefix={<BarChartOutlined />}
                />
              </Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]} className="mt16">
            <Col xs={24} xl={16}>
              <Card title="Biểu đồ doanh thu theo ngày" className="shop-revenue__card">
                <ResponsiveContainer width="100%" height={320}>
                  <LineChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="label" />
                    <YAxis />
                    <Tooltip formatter={(value) => money(value)} />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="revenue"
                      stroke="#1677ff"
                      strokeWidth={3}
                      name="Doanh thu"
                    />
                  </LineChart>
                </ResponsiveContainer>
              </Card>
            </Col>

            <Col xs={24} xl={8}>
              <Card title="Số đơn theo ngày" className="shop-revenue__card">
                <ResponsiveContainer width="100%" height={320}>
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="label" />
                    <YAxis />
                    <Tooltip formatter={(value) => number(value)} />
                    <Legend />
                    <Bar dataKey="totalOrders" fill="#1677ff" name="Tổng đơn" />
                    <Bar dataKey="deliveredOrders" fill="#52c41a" name="Hoàn thành" />
                  </BarChart>
                </ResponsiveContainer>
              </Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]} className="mt16">
            <Col xs={24} xl={8}>
              <Card title="Top sản phẩm doanh thu cao" className="shop-revenue__card">
                {topProducts.length === 0 ? (
                  <Empty description="Chưa có dữ liệu sản phẩm" />
                ) : (
                  <div className="shop-revenue__top-list">
                    {topProducts.map((item) => (
                      <div key={item.productId} className="shop-revenue__top-item">
                        <div className="shop-revenue__top-left">
                          <img
                            src={item.image || "https://via.placeholder.com/56"}
                            alt={item.productName}
                            className="shop-revenue__top-image"
                          />
                          <div>
                            <div className="shop-revenue__top-name">
                              {item.productName}
                            </div>
                            <div className="shop-revenue__top-meta">
                              Đã bán {number(item.soldCount)} sản phẩm
                            </div>
                          </div>
                        </div>
                        <div className="shop-revenue__top-revenue">
                          {money(item.revenue)}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </Card>
            </Col>

            <Col xs={24} xl={16}>
              <Card title="Chi tiết doanh thu" className="shop-revenue__card">
                <Table
                  rowKey="label"
                  columns={detailColumns}
                  dataSource={detailRows}
                  pagination={false}
                  scroll={{ x: 800 }}
                />
              </Card>
            </Col>
          </Row>
        </>
      )}
    </div>
  );
}

export default ShopRevenue;