import "./OtpPassword.scss"
import { PiSealCheckBold } from "react-icons/pi";
import { MdOutlineLocalShipping } from "react-icons/md";
import { MdOutlineSupportAgent } from "react-icons/md";
import { IoArrowBackOutline } from "react-icons/io5";
import { HiOutlineCheckBadge } from "react-icons/hi2";
import { Link, useNavigate } from "react-router-dom"
import { Input } from "antd";
import { useState } from "react";
import { post } from "../../../utils/request"


function OtpPassword() {
  const navigate = useNavigate();
  const [error, setError] = useState("");
  const [otp, setOtp] = useState(null);

  const email = localStorage.getItem("email");

  const handleVerify = () => {

    const fetchApi = async () => {
      
      try {
        const res = await post("auth/verify-otp", {
          email: email,
          otp: otp
        })

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        const data = await res.json();

        localStorage.removeItem("email");
        localStorage.setItem("resetToken", data.resetToken);
        navigate('/dat-lai-mat-khau')
      } catch (error) {
        setError(error.message);
      }
    }
    fetchApi();
  }

  return (
    <>
      <div className="otp-password">
        <div className="otp-password__left">
          <div className="otp-password__left__logo">
            {/* <MdShoppingBag />
            Chợ Số */}
          </div>
          <div className="otp-password__left__content">
            <h1 className="otp-password__left__title">
              Xác thực tài khoản
            </h1>
            <p className="otp-password__left__desc">
              Vui lòng nhập mã OTP để xác nhận danh tính và tiếp tục quá trình bảo mật của bạn
            </p>
          </div>
          <div className="otp-password__left__footer">
            <div className="otp-password__left__footer-item">
              <PiSealCheckBold />
              <span>Hàng chính hãng</span>
            </div>
            <div className="otp-password__left__footer-item">
              <MdOutlineLocalShipping />
              <span>Giao hàng nhanh</span>
            </div>
            <div className="otp-password__left__footer-item">
              <MdOutlineSupportAgent />
              <span>Hỗ trợ 24/7</span>
            </div>
          </div>
        </div>

        <div className="otp-password__right">
          <Link to='/quen-mat-khau' className="otp-password__back">
            <IoArrowBackOutline />
            <span>Quay lại</span>
          </Link>
          <div className="otp-password__title">
            Nhập mã xác thực
          </div>
          <div className="otp-password__desc">
            Mã OTP đã được gửi về email của bạn. Vui lòng nhập mã gồm 6 chữ số bên dưới
          </div>

          <Input.OTP
            length={6}
            className="otp-password__input"
            size="large"
            separator={<span style={{ color: 'blue' }}>—</span>}
            onChange={(e) => setOtp(e)}
          />

          {error && (
            <div className="otp-password__error">
              {/* <MdErrorOutline /> */}
              <p>{error}</p>
            </div>
          )}

          <button className="otp-password__btn" onClick={handleVerify}>
            Xác thực ngay <HiOutlineCheckBadge />
          </button>
        </div>
      </div>
    </>
  )
}

export default OtpPassword;