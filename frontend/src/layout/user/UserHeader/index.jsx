import "./UserHeader.scss";
import { AiOutlineHome } from "react-icons/ai";
import { FaRegUserCircle } from "react-icons/fa";
import { IoCartOutline } from "react-icons/io5";
import { IoIosSearch } from "react-icons/io";
import { BiSearchAlt } from "react-icons/bi";
import { Link, useNavigate } from "react-router-dom"
import { useState } from "react";
import { useContext } from "react";
import { CartContext } from "../../../components/CartContext";
import { message, notification } from "antd";
import { post } from "../../../utils/request";


function UserHeader() {
  const [openDrop, setOpenDrop] = useState(false);
  const [openDropAcc, setOpenDropAcc] = useState(false);
  const navigate = useNavigate();
  const [keyword, setKeyword] = useState("");

  const fullname = localStorage.getItem("fullname");
  const avatar = localStorage.getItem("avatar");

  const { cartVariants } = useContext(CartContext);
  const token = localStorage.getItem("token");

  const handleSearch = () => {
      if (!keyword.trim()) return;
      navigate(`/tim-kiem?keyword=${encodeURIComponent(keyword)}`);
      setOpenDrop(false);
    };

  const handleCartClick = (e) => {
    if (!token) {
      e.preventDefault(); // ngăn Link mặc định
      message.warning({
        content: "Vui lòng đăng nhập để xem giỏ hàng"
      });
      return;
    }
    navigate("/gio-hang"); // nếu đã đăng nhập, chuyển sang trang giỏ hàng
  };

  const handleLogout = async () => {
    try {
      await post("auth/logout"); 
    } catch (err) {
      console.log("Logout API lỗi", err);
    }

    localStorage.removeItem("token");
    localStorage.removeItem("fullname");
    localStorage.removeItem("email");
    localStorage.removeItem("avatar");
    localStorage.removeItem("roles");
    localStorage.removeItem("shopId");

    sessionStorage.removeItem("token");

    navigate("/");

    window.location.reload();
  };

  return (
    <>
      <div className="user-header">
        <div className="container">
          <Link to="/" className="user-header__logo">
            <img src="https://res.cloudinary.com/dcjraarbb/image/upload/v1772208191/Ch%E1%BB%A3_S%E1%BB%91_fdztr0.png" alt="Chợ tốt" />
          </Link>
          <div className="user-header__content">
            <div className="user-header__content-top">
            <div className="user-header__search">
                <IoIosSearch />
                <input
                  type="text"
                  placeholder="Áo thun"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  // onFocus={() => setOpenDrop(true)}
                  onBlur={() => {
                    setTimeout(() => setOpenDrop(false), 200);
                  }}
                ></input>

                <button onClick={handleSearch}>Tìm kiếm</button>

                {/* {openDrop && (
                  <>
                    <div className="user-header__search__drop">
                      <div className="user-header__search__drop__item">
                        <BiSearchAlt />
                        nguyen manh kha
                      </div>
                      <div className="user-header__search__drop__item">
                        <BiSearchAlt />
                        nguyen minh duc
                      </div>
                    </div>
                  </>
                )} */}
              </div>
              <Link to="/" className="user-header__item">
                <AiOutlineHome className="user-header__item-icon" />
                <div className="user-header__item-text">
                  Trang chủ
                </div>
              </Link>

              {fullname ? (
                <div
                  className="user-header__item account"
                  onMouseEnter={() => setOpenDropAcc(true)}
                  onMouseLeave={() => setOpenDropAcc(false)}
                >
                  <img src={avatar} alt="" className="user-header__item__avatar" />
                  <div className="user-header__item-text">
                    {fullname}
                  </div>

                  {openDropAcc && (
                    <div className="account__drop">
                      <div className="account__drop__item">
                        <Link to="/tai-khoan">
                          Hồ sơ của tôi
                        </Link>
                      </div>
                      <div className="account__drop__item">
                        <button className="logout" onClick={handleLogout}>
                          Đăng xuất
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              ) : (
                <Link to="/dang-nhap" className="user-header__item">
                  <FaRegUserCircle className="user-header__item-icon" />
                  <div className="user-header__item-text">
                    Tài khoản
                  </div>
                </Link>
              )}

              <div className="user-header__cart" onClick={handleCartClick}>
                <div className="user-header__cart__badge">
                  {cartVariants.length}
                </div>
                <IoCartOutline />
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default UserHeader;