import "./LayoutShop.scss";
import { Layout, Menu, Badge, Dropdown, notification } from "antd";
import Sider from "antd/es/layout/Sider";
import { MdDashboard } from "react-icons/md";
import { Content } from "antd/es/layout/layout";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { BsFillInboxesFill } from "react-icons/bs";
import { FaShoppingCart, FaBell } from "react-icons/fa";
import { MdDisplaySettings } from "react-icons/md";
import { MdOutlineFlashOn } from "react-icons/md";
import { useEffect, useMemo, useRef, useState } from "react";
import { FaChartPie } from "react-icons/fa";
import { MdRateReview } from "react-icons/md";
import { connectSocket, disconnectSocket, subscribeSocket } from "../../utils/socket";
import { get } from "../../utils/request";
import { Avatar, Button } from "antd";
import { LogoutOutlined, UserOutlined } from "@ant-design/icons";
import { post } from "../../utils/request";
import ChatWidget from "../../components/ChatWidget";

function LayoutShop() {
  const location = useLocation();
  const navigate = useNavigate();
  const subscriptionRef = useRef(null);

  const userId = Number(localStorage.getItem("userId"));

  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const path = location.pathname.substring(6);

  let title = "";
  switch (path) {
    case "":
      title = "Dashboard Cửa hàng";
      break;
    case "products":
      title = "Sản phẩm";
      break;
    case "orders":
      title = "Đơn hàng";
      break;
    case "flash-sale":
      title = "Flash Sale";
      break;
    case "reviews":
      title = "Đánh giá";
      break;
    case "revenue":
      title = "Doanh thu";
      break;
    case "profile":
      title = "Cấu hình shop";
      break;
    default:
      title = "Shop";
  }

  const selectedKeys = useMemo(() => {
    if (location.pathname === "/shop") return ["dashboard"];
    if (location.pathname.startsWith("/shop/products") || location.pathname.startsWith("/shop/edit-product")) return ["products"];
    if (location.pathname.startsWith("/shop/orders")) return ["orders"];
    if (location.pathname.startsWith("/shop/flash-sale")) return ["flash-sale"];
    if (location.pathname.startsWith("/shop/reviews")) return ["review"];
    if (location.pathname.startsWith("/shop/revenue")) return ["revenue"];
    if (location.pathname.startsWith("/shop/profile")) return ["profile"];
    return ["dashboard"];
  }, [location.pathname]);

  const items = [
    {
      key: "dashboard",
      label: <Link to="/shop">Dashboard</Link>,
      icon: <MdDashboard />
    },
    {
      key: "products",
      label: <Link to="/shop/products">Sản phẩm</Link>,
      icon: <BsFillInboxesFill />
    },
    {
      key: "orders",
      label: <Link to="/shop/orders">Đơn hàng</Link>,
      icon: <FaShoppingCart />
    },
    {
      key: "flash-sale",
      label: <Link to="/shop/flash-sale">Flash Sale</Link>,
      icon: <MdOutlineFlashOn />
    },
    {
      key: "revenue",
      label: <Link to="/shop/revenue">Thống kê doanh thu</Link>,
      icon: <FaChartPie />
    },
    {
      key: "review",
      label: <Link to="/shop/reviews">Đánh giá</Link>,
      icon: <MdRateReview />
    },
    {
      key: "profile",
      label: <Link to="/shop/profile">Cấu hình shop</Link>,
      icon: <MdDisplaySettings />
    }
  ];

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const res = await get("notifications/my");
        const data = await res.json();
        setNotifications(data);
        setUnreadCount(data.filter((item) => !item.isRead).length);
      } catch (error) {
        console.error("Lỗi lấy thông báo:", error);
      }
    };

    fetchNotifications();
  }, []);

  console.log(notifications);

  useEffect(() => {
    if (!userId) return;

    connectSocket(() => {
      subscriptionRef.current = subscribeSocket(
        `/topic/user/${userId}/notifications`,
        (data) => {
          if (!data) return;

          setNotifications((prev) => [data, ...prev]);
          setUnreadCount((prev) => prev + 1);

          notification.success({
            message: data.title || "Thông báo mới",
            description: data.message,
            placement: "topRight",
            onClick: () => {
              if (data.type === "NEW_ORDER" && data.orderId) {
                navigate(`/shop/orders/${data.orderId}`);
              } else if (
                data.type === "PRODUCT_APPROVED" ||
                data.type === "PRODUCT_REJECTED"
              ) {
                navigate("/shop/products");
              } else if (
                data.type === "NEW_REVIEW" ||
                data.type === "BAD_REVIEW"
              ) {
                navigate("/shop/reviews");
              } else {
                navigate("/shop");
              }
            }
          });
        }
      );
    });

    return () => {
      if (subscriptionRef.current?.unsubscribe) {
        subscriptionRef.current.unsubscribe();
      }
      disconnectSocket();
    };
  }, [userId, navigate]);

  const handleOpenNotifications = () => {
    setUnreadCount(0);
    setNotifications((prev) =>
      prev?.map((item) => ({ ...item, isRead: true }))
    );
  };

  const notificationMenuItems = notifications.length
    ? notifications.map((item, index) => ({
      key: `${item.id || item.orderId || index}`,
      label: (
        <div
          style={{ maxWidth: 480 }}
          // onClick={() => {
          //   // if (item.orderId) {
          //   //   navigate(`/shop/orders/${item.orderId}`);
          //   // } else {
          //   navigate("/shop/orders");
          //   // }
          // }}
        >
          <div style={{ fontWeight: 600 }}>
            {item.title || (item.type === "NEW_ORDER" ? "Đơn hàng mới" : "Thông báo")}
          </div>
          <div>{item.message}</div>
          {item.orderCode && (
            <div style={{ fontSize: 12, color: "#888", marginTop: 4 }}>
              Mã đơn: {item.orderCode}
            </div>
          )}
        </div>
      )
    }))
    : [
      {
        key: "empty",
        label: <div>Chưa có thông báo</div>
      }
    ];

  const username =
    localStorage.getItem("fullname") ||
    localStorage.getItem("username") ||
    "Người dùng";

  const avatar = localStorage.getItem("avatar");

  const handleLogout = async () => {
    try {
      await post("auth/logout");
    } catch (err) {
      console.log("Logout API lỗi", err);
    }

    localStorage.clear();
    navigate("/dang-nhap");
  };

  return (
    <Layout>
      <Sider className="app-sider" theme="light" width={220}>
        <Link to={"/"} className="layout-admin__logo">
          <img src="https://res.cloudinary.com/dcjraarbb/image/upload/v1772208191/Ch%E1%BB%A3_S%E1%BB%91_fdztr0.png" alt="" />
        </Link>

        <Menu
          mode="inline"
          selectedKeys={selectedKeys}
          items={items}
          className="app-sider__menu"
        />

        {/* USER */}
        <div className="app-sider__user">
          <div className="app-sider__profile">
            <Avatar
              size={42}
              src={avatar}
              icon={!avatar && <UserOutlined />}
              className="app-sider__avatar"
            />

            <div className="app-sider__info">
              <div className="app-sider__name">{username}</div>
              <div className="app-sider__role">Cửa hàng</div>
            </div>
          </div>

          <Button
            danger
            block
            icon={<LogoutOutlined />}
            onClick={handleLogout}
            className="app-sider__logout"
          >
            Đăng xuất
          </Button>
        </div>
      </Sider>

      <Layout>
        <div className="layout-admin__header">
          <div className="layout-admin__header__title">{title}</div>

          <div className="layout-admin__header__right">
            <Dropdown
              menu={{ items: notificationMenuItems }}
              trigger={["click"]}
              placement="bottomRight"
              onOpenChange={(open) => {
                if (open) handleOpenNotifications();
              }}
            >
              <Badge count={unreadCount} size="small">
                <div className="layout-admin__notification">
                  <FaBell />
                </div>
              </Badge>
            </Dropdown>
          </div>
        </div>

        <Content style={{ overflow: "initial", padding: 24, background: "#f6f6f8" }}>
          <Outlet />
        </Content>
      </Layout>
      <ChatWidget />
    </Layout>
  );
}

export default LayoutShop;