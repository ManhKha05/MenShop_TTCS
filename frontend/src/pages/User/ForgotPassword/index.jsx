import "./ForgotPassword.scss"
import { PiSealCheckBold } from "react-icons/pi";
import { MdOutlineLocalShipping } from "react-icons/md";
import { MdOutlineSupportAgent } from "react-icons/md";
import { MdShoppingBag } from "react-icons/md";
import { MdErrorOutline } from "react-icons/md";
import { BsEnvelopeArrowUpFill } from "react-icons/bs";
import { PiIdentificationCard } from "react-icons/pi";
import { IoArrowBackOutline } from "react-icons/io5";
import { Link, useNavigate } from "react-router-dom"
import { useState } from "react";
import { post } from "../../../utils/request";
import { notification } from "antd";


function ForgotPassword() {
  const navigate = useNavigate();
  const [notificationApi, contextHolder] = notification.useNotification();
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");


  const handleClick = () => {
    if (!email.trim()) {
      setError("Vui lòng nhập email")
      return;
    }

    const fetchApi = async () => {
      try {
        const res = await post("auth/send-otp", { email });

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        notification.success({
          message: "Thành công",
          description: "OTP đã được gửi về email",
          showProgress: true
        });

        localStorage.setItem("email", email);
        navigate("/xac-thuc-otp");

      } catch (error) {
        setError(error.message)
      }
    }
    fetchApi();
  }

  return (
    <>
      {contextHolder}
      <div className="forgot-password">
        <div className="forgot-password__left">
          <div className="forgot-password__left__logo">
            {/* <MdShoppingBag />
            Chợ Số */}
          </div>
          <div className="forgot-password__left__content">
            <h1 className="forgot-password__left__title">
              Đừng lo, chúng tôi <br /> ở đây để giúp bạn <br /> lấy lại mật khẩu
            </h1>
            <p className="forgot-password__left__desc">
              Chỉ mất vài bước đơn giản để bảo vệ và khôi phục quyền truy cập vào tài khoản của bạn
            </p>
          </div>
          <div className="forgot-password__left__footer">
            <div className="forgot-password__left__footer-item">
              <PiSealCheckBold />
              <span>Hàng chính hãng</span>
            </div>
            <div className="forgot-password__left__footer-item">
              <MdOutlineLocalShipping />
              <span>Giao hàng nhanh</span>
            </div>
            <div className="forgot-password__left__footer-item">
              <MdOutlineSupportAgent />
              <span>Hỗ trợ 24/7</span>
            </div>
          </div>
        </div>

        <div className="forgot-password__right">
          <Link to='/dang-nhap' className="forgot-password__back">
            <IoArrowBackOutline />
            <span>Quay lại Đăng nhập</span>
          </Link>
          <div className="forgot-password__title">
            Quên mật khẩu?
          </div>
          <div className="forgot-password__desc">
            Vui lòng nhập email liên kết với tài khoản của bạn để nhận mã xác minh
          </div>

          <div className="forgot-password-form">
            <label htmlFor="email">
              Email
            </label>
            <div className="forgot-password-form__input ">
              <PiIdentificationCard className="forgot-password-form__input__icon" />
              <input
                type="text"
                placeholder="abc@gmail.com"
                id="email"
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            {error && (
              <div className="forgot-password__error">
                <MdErrorOutline />
                <p>{error}</p>
              </div>
            )}

            <button
              className="forgot-password-form__btn"
              onClick={handleClick}
            >
              Gửi mã xác nhận <BsEnvelopeArrowUpFill />
            </button>
          </div>

          {/* <div className="forgot-password__text">
            Chưa nhận được mã?
            <Link to="/quen-mat-khau">Gửi lại mã</Link>
          </div> */}
        </div>
      </div>
    </>
  )
}

export default ForgotPassword;