import Sider from "antd/es/layout/Sider";
import { MdDashboard } from "react-icons/md";
import { FaUsersGear } from "react-icons/fa6";
import { Content } from "antd/es/layout/layout";
import "./LayoutAdmin.scss";
import { Link, Outlet, useLocation, useNavigate } from "react-router-dom";
import { FaShop } from "react-icons/fa6";
import { BiSolidCategory } from "react-icons/bi";
import { BsFillInboxesFill } from "react-icons/bs";
import { FaShoppingCart } from "react-icons/fa";
import { FaAd } from "react-icons/fa";
import { MdOutlineFlashOn } from "react-icons/md";
import { LogoutOutlined, UserOutlined } from "@ant-design/icons";
import { Avatar, Badge, Button, Dropdown, Layout, Menu, notification } from "antd";
import { FaBell } from "react-icons/fa";
import { useEffect, useRef, useState } from "react";
import { get, post } from "../../utils/request";
import { connectSocket, disconnectSocket, subscribeSocket } from "../../utils/socket";


function LayoutAdmin() {
  const navigate = useNavigate();
  let location = useLocation();
  location = location.pathname;

  let title = "";

  switch (location.substring(7)) {
    case "":
      title = "Dashboard"
      break

    case "shops":
      title = "Quản lí cửa hàng"
      break

    case "categories":
      title = "Quản lí danh mục"
      break

    case "products":
      title = "Quản lí sản phẩm"
      break

    case "users":
      title = "Quản lí người dùng"
      break

    case "flash-sale":
      title = "Flash Sale"
      break

    case "banners":
      title = "Quản lí Banner"
      break

  }

  const items = [
    {
      key: 'dashboard',
      label: <Link to='/admin'>Dashboard</Link>,
      icon: <MdDashboard />
    },
    {
      key: 'shops',
      label: <Link to='/admin/shops'>Cửa hàng</Link>,
      icon: <FaShop />
    },
    {
      key: 'categories',
      label: <Link to='/admin/categories'>Danh mục</Link>,
      icon: <BiSolidCategory />
    },
    {
      key: 'products',
      label: <Link to='/admin/products'>Sản phẩm</Link>,
      icon: <BsFillInboxesFill />
    },
    {
      key: 'users',
      label: <Link to='/admin/users'>Người dùng</Link>,
      icon: <FaUsersGear />
    },
    // {
    //   key: 'orders',
    //   label: <Link to='/admin/orders'>Đơn hàng</Link>,
    //   icon: <FaShoppingCart />
    // },
    {
      key: 'flash-sale',
      label: <Link to='/admin/flash-sale'>Flash Sale</Link>,
      icon: <MdOutlineFlashOn />
    },
    {
      key: 'banners',
      label: <Link to='/admin/banners'>Banner</Link>,
      icon: <FaAd />
    }
  ]

  const getSelectedKey = (pathname) => {
    if (pathname === "/admin") return "dashboard";
    if (pathname.startsWith("/admin/shops")) return "shops";
    if (pathname.startsWith("/admin/categories")) return "categories";
    if (pathname.startsWith("/admin/products")) return "products";
    if (pathname.startsWith("/admin/users")) return "users";
    if (pathname.startsWith("/admin/flash-sale")) return "flash-sale";
    if (pathname.startsWith("/admin/banners")) return "banners";
    return "dashboard";
  };

  const username = localStorage.getItem("fullname") || localStorage.getItem("username") || "Người dùng";
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

  const subscriptionRef = useRef(null);
  const userId = Number(localStorage.getItem("userId"));

  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const res = await get("notifications/my");
        const data = await res.json();

        setNotifications(Array.isArray(data) ? data : []);
        setUnreadCount(
          Array.isArray(data)
            ? data.filter((item) => !item.isRead).length
            : 0
        );
      } catch (error) {
        console.error("Lỗi lấy thông báo admin:", error);
      }
    };

    fetchNotifications();
  }, []);

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
            onClick: () => handleNotificationNavigate(data)
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
  }, [userId]);

  const handleNotificationNavigate = (item) => {
    if (!item) return;

    if (item.type === "NEW_PRODUCT_PENDING") {
      navigate("/admin/products");
    } else if (item.type === "NEW_SHOP_REGISTER") {
      navigate("/admin/shops");
    } else if (item.type === "NEW_FLASHSALE_REQUEST") {
      navigate("/admin/flash-sale");
    } else if (item.type === "REPORT_PRODUCT") {
      navigate("/admin/products");
    } else if (item.type === "REPORT_SHOP") {
      navigate("/admin/shops");
    } else if (item.type === "NEW_USER_REGISTER") {
      navigate("/admin/users");
    } else {
      navigate("/admin");
    }
  };

  const handleOpenNotifications = () => {
    setUnreadCount(0);
    setNotifications((prev) =>
      prev.map((item) => ({ ...item, isRead: true }))
    );
  };

  const notificationMenuItems = notifications.length
    ? notifications.map((item, index) => ({
      key: `${item.id || index}`,
      label: (
        <div
          className="admin-notification-item"
          onClick={() => handleNotificationNavigate(item)}
        >
          <div className="admin-notification-item__title">
            {item.title || "Thông báo"}
          </div>

          <div className="admin-notification-item__message">
            {item.message}
          </div>

          {item.createdAt && (
            <div className="admin-notification-item__time">
              {new Date(item.createdAt).toLocaleString("vi-VN")}
            </div>
          )}
        </div>
      )
    }))
    : [
      {
        key: "empty",
        label: <div className="admin-notification-empty">Chưa có thông báo</div>
      }
    ];

  return (
    <>
      <Layout>
        <Sider className="app-sider" theme="light" width={220} >
          <div className="layout-admin__logo">
            <img src="https://res.cloudinary.com/dcjraarbb/image/upload/v1772208191/Ch%E1%BB%A3_S%E1%BB%91_fdztr0.png" alt="" />
          </div>
          <Menu
            mode="inline"
            selectedKeys={[getSelectedKey(location)]}
            items={items}
            className="app-sider__menu"
          />

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
                <div className="app-sider__role">
                  {location.startsWith("/admin") ? "Quản trị viên" : "Cửa hàng"}
                </div>
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
        <Layout >
          <div className="layout-admin__header">
            <div className="layout-admin__header__title">
              {title}
            </div>

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
          <Content style={{ overflow: 'initial', padding: 24, background: '#f6f6f8' }}>
            <Outlet />
          </Content>
          {/* <Footer style={{ textAlign: 'center' }}>
            Ant Design ©{new Date().getFullYear()} Created by Ant UED
          </Footer> */}
        </Layout>
      </Layout>
    </>
  )
}

export default LayoutAdmin;