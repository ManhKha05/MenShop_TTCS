import "./AccountLayout.scss";
import { FaRegUser } from "react-icons/fa";
import { IoNewspaperOutline } from "react-icons/io5";
import { FaOpencart } from "react-icons/fa";
import { Link, Outlet, useLocation } from "react-router-dom";
import { FaMapLocationDot } from "react-icons/fa6";

function AccountLayout() {
  const location = useLocation();

  const roles = localStorage.getItem("roles");

  return (
    <>
      <div className="account-layout">
        <div className="container">
          <div className="account-layout__nav">
            <Link
              to="/tai-khoan"
              className={"account-layout__nav__item " + (location.pathname === '/tai-khoan' ? "active" : "")}
            >
              <FaRegUser />
              <span>Hồ sơ của tôi</span>
            </Link>
            <Link
              to="don-mua"
              className={"account-layout__nav__item " + (location.pathname === '/tai-khoan/don-mua' ? "active" : "")}
            >
              <IoNewspaperOutline />
              <span>Đơn mua</span>
            </Link>
            <Link
              to="dia-chi"
              className={"account-layout__nav__item " + (location.pathname === '/tai-khoan/dia-chi' ? "active" : "")}
            >
              <FaMapLocationDot />
              <span>Địa chỉ của tôi</span>
            </Link>
            {!roles.includes("ROLE_SHOP") && (
              <Link
                to="dang-ky-ban-hang"
                className={"account-layout__nav__item " + (location.pathname === '/tai-khoan/dang-ky-ban-hang' ? "active" : "")}
              >
                <FaOpencart />
                <span>Đăng ký bán hàng</span>
              </Link>
            )}

          </div>

          <div className="account-layout__main">
            <Outlet />
          </div>
        </div>
      </div>
    </>
  )
}

export default AccountLayout;