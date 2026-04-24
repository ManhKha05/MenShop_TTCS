import "./ResetPassword.scss"
import { PiSealCheckBold } from "react-icons/pi";
import { MdOutlineLocalShipping, MdOutlineLock } from "react-icons/md";
import { MdOutlineSupportAgent } from "react-icons/md";
import { MdShoppingBag } from "react-icons/md";
import { MdErrorOutline } from "react-icons/md";
import { MdLockReset } from "react-icons/md";
import { useState } from "react";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { post } from "../../../utils/request";
import { useNavigate } from "react-router-dom";
import { notification } from "antd";


function ResetPassword() {
  const navigate = useNavigate();
  const [notificationApi, contextHolder] = notification.useNotification();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [password, setPassword] = useState();
  const [confirmPassword, setConfirmPassword] = useState();
  const [errors, setErrors] = useState({});

  const resetToken = localStorage.getItem("resetToken");

  const validate = () => {
    const newErrors = {};
    if (!password) newErrors.password = "Vui lòng nhập mật khẩu";
    else if (password.length < 8) newErrors.password = "Mật khẩu phải có ít nhất 8 ký tự";
    else if (confirmPassword !== password) newErrors.confirmPassword = "Mật khẩu không khớp";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0
  }



  const handleSubmit = () => {
    if (!validate()) return;

    const fetchApi = async () => {

      try {
        const res = await post("auth/reset-password", {
          newPassword: password,
          resetToken
        })

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        const data = await res.json();

        notification.success({
          message: "Thành công",
          description: data.message,
          showProgress: true,
        });

        localStorage.removeItem("resetToken");
        navigate('/dang-nhap')
      } catch (error) {
        notificationApi.error({
          title: "Thất bại",
          description: error.message || "Có lỗi xảy ra, thử lại sau",
          showProgress: true,
        });
      }
    }
    fetchApi();
  }

  return (
    <>
      {contextHolder}
      <div className="reset-password">
        <div className="reset-password__left">
          <div className="reset-password__left__logo">
            {/* <MdShoppingBag />
            Chợ Số */}
          </div>
          <div className="reset-password__left__content">
            <h1 className="reset-password__left__title">
              Thiết lập mật khẩu mới
            </h1>
            <p className="reset-password__left__desc">
              Đảm bảo tài khoản của bạn luôn an toàn với mật khẩu có độ bảo mật cao
            </p>
          </div>
          <div className="reset-password__left__footer">
            <div className="reset-password__left__footer-item">
              <PiSealCheckBold />
              <span>Hàng chính hãng</span>
            </div>
            <div className="reset-password__left__footer-item">
              <MdOutlineLocalShipping />
              <span>Giao hàng nhanh</span>
            </div>
            <div className="reset-password__left__footer-item">
              <MdOutlineSupportAgent />
              <span>Hỗ trợ 24/7</span>
            </div>
          </div>
        </div>

        <div className="reset-password__right">
          <div className="reset-password__title">
            Tạo mật khẩu mới
          </div>
          <div className="reset-password__desc">
            Mật khẩu mới của bạn phải khác với mật khẩu đã sử dụng trước đó
          </div>

          <div className="reset-password-form">
            <label htmlFor="password">
              Mật khẩu mới
            </label>
            <div className="reset-password-form__input ">
              <MdOutlineLock className="reset-password-form__input__icon" />
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Mật khẩu"
                id="password"
                onChange={(e) => setPassword(e.target.value)}
              />
              <button
                type="button"
                className="reset-password-form__input__icon"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <LuEye /> : <LuEyeOff />}
              </button>
            </div>

            {errors.password && (
              <div className="reset-password__error">
                <MdErrorOutline />
                <p>{errors.password}</p>
              </div>
            )}

            <label htmlFor="confirmPassword">
              Xác nhận mật khẩu mới
            </label>
            <div className="reset-password-form__input ">
              <MdLockReset className="reset-password-form__input__icon" />
              <input
                type={showConfirmPassword ? "text" : "password"}
                placeholder="Nhập lại mật khẩu"
                id="confirmPassword"
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              <button
                type="button"
                className="reset-password-form__input__icon"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                {showConfirmPassword ? <LuEye /> : <LuEyeOff />}
              </button>
            </div>

            {errors.confirmPassword && (
              <div className="reset-password__error">
                <MdErrorOutline />
                <p>{errors.confirmPassword}</p>
              </div>
            )}

            <button className="reset-password-form__btn" onClick={handleSubmit}>
              Đổi mật khẩu <MdLockReset />
            </button>
          </div>
        </div>
      </div>
    </>
  )
}

export default ResetPassword;