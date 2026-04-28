import ChatWidget from "../../components/ChatWidget";
import UserFooter from "./UserFooter";
import UserHeader from "./UserHeader";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { FloatButton } from "antd";
import { DashboardOutlined } from "@ant-design/icons";

function LayoutHome() {
  const location = useLocation();
  const navigate = useNavigate();

  const roles = JSON.parse(localStorage.getItem("roles") || "[]");

  const isCustomerPage =
    !location.pathname.startsWith("/admin") &&
    !location.pathname.startsWith("/shop");

  const canAccessDashboard =
    roles.includes("ROLE_ADMIN") || roles.includes("ROLE_SHOP");

  const goToDashboard = () => {
    if (roles.includes("ROLE_ADMIN")) {
      navigate("/admin");
    } else if (roles.includes("ROLE_SHOP")) {
      navigate("/shop");
    }
  };
  return (
    <>
      <UserHeader />
      {isCustomerPage && canAccessDashboard && (
        <FloatButton
          icon={<DashboardOutlined />}
          shape="square"
          tooltip={
            roles.includes("ROLE_ADMIN")
              ? "Trang quản trị"
              : "Trang cửa hàng"
          }
          // content={
          //   roles.includes("ROLE_ADMIN")
          //     ? "Trang quản trị"
          //     : "Trang cửa hàng"
          // }
          onClick={goToDashboard}
          style={{
            right: 24,
            bottom: 70,
            // insetInlineEnd: 164
          }}
        />
      )}
      <div style={{ marginTop: '95px', backgroundColor: '#f6f6f8' }}>
        <Outlet />
      </div>
      <UserFooter />
      <ChatWidget />
    </>
  )
}

export default LayoutHome;