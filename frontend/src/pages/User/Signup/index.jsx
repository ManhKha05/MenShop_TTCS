import { useState } from "react";
import "./Signup.scss"
import { MdErrorOutline, MdOutlineLocalShipping, MdOutlineLock, MdOutlineSupportAgent, MdShoppingBag } from "react-icons/md";
import { PiSealCheckBold } from "react-icons/pi";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { IoIosArrowRoundForward } from "react-icons/io";
import { FcGoogle } from "react-icons/fc";
import { Link } from "react-router-dom";
import { FaRegUser } from "react-icons/fa6";
import { HiIdentification } from "react-icons/hi2";
import { RiRotateLockFill } from "react-icons/ri";
import { IoPersonAddSharp } from "react-icons/io5";
import { post } from "../../../utils/request";
import { notification } from "antd";
import { MdEmail } from "react-icons/md";


function Signup() {
  const [notificationApi, contextHolder] = notification.useNotification();
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [formData, setFormData] = useState({
    fullname: "",
    email: "",
    password: "",
    confirmPassword: ""
  });
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.id]: e.target.value
    }));
  }

  const validate = () => {
    const newErrors = {};
    if (!formData.fullname.trim()) newErrors.fullname = "Vui lòng nhập họ và tên";
    if (!formData.email.trim()) newErrors.email = "Vui lòng nhập email";
    // if (!formData.username.trim()) newErrors.username = "Vui lòng nhập tên đăng nhập";
    if (!formData.password) newErrors.password = "Vui lòng nhập mật khẩu";
    else if (formData.password.length < 8) newErrors.password = "Mật khẩu phải có ít nhất 8 ký tự";
    if (formData.confirmPassword !== formData.password) newErrors.confirmPassword = "Mật khẩu không khớp";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  const handleSignUp = (e) => {
    e.preventDefault();

    if (!validate()) return;
    console.log(formData);

    const fetchApi = async () => {
      try {
        const res = await post("auth/sign-up", formData);

        if (!res.ok) {
          const err = await res.json();
          throw err.message;
        }

        notificationApi.success({
          title: "Đăng ký thành công",
          description: "Bạn đã tạo tài khoản thành công, hãy đăng nhập ngay!"
        });
      } catch (error) {
        console.error(error);
        notificationApi.error({
          title: "Đăng ký thất bại",
          description: error || "Có lỗi xảy ra, thử lại sau"
        });
      }
    }
    fetchApi();
  }

  return (
    <>
      {contextHolder}
      <div className="signup">
        <div className="signup__left">
          <div className="signup__left__logo">
            {/* <MdShoppingBag />
            Chợ Số */}
          </div>
          <div className="signup__left__content">
            <h1 className="signup__left__title">
              Tham gia <br /> cộng đồng Chợ Số
            </h1>
            <p className="signup__left__desc">
              Nhận ngay ngàn ưu đãi hấp dẫn và trải nghiệm mua sắm không giới hạn
            </p>
          </div>
          <div className="signup__left__footer">
            <div className="signup__left__footer-item">
              <PiSealCheckBold />
              <span>Hàng chính hãng</span>
            </div>
            <div className="signup__left__footer-item">
              <MdOutlineLocalShipping />
              <span>Giao hàng nhanh</span>
            </div>
            <div className="signup__left__footer-item">
              <MdOutlineSupportAgent />
              <span>Hỗ trợ 24/7</span>
            </div>
          </div>
        </div>

        <div className="signup__right">
          <div className="signup__title">
            Đăng ký tài khoản
          </div>
          <div className="signup__desc">
            Trở thành thành viên để nhận nhiều ưu đãi hơn
          </div>

          <form className="signup-form" onSubmit={handleSignUp}>
            <label htmlFor="fullname">
              Họ và tên
            </label>
            <div className={"signup-form__input " + (errors.fullname && "signup-form__input-error")}>
              <HiIdentification className="signup-form__input__icon" />
              <input
                type="text"
                placeholder="Nguyễn Văn A"
                id="fullname"
                onChange={handleChange}
              />
            </div>

            {errors.fullname && (
              <div className="signup__error">
                <MdErrorOutline />
                <p>{errors.fullname}</p>
              </div>
            )}

            <label htmlFor="email">
              Email
            </label>
            <div className={"signup-form__input " + (errors.email && "signup-form__input-error")}>
              <MdEmail  className="signup-form__input__icon" />
              <input
                type="text"
                placeholder="abc@gmail.com"
                id="email"
                onChange={handleChange}
              />
            </div>

            {errors.email && (
              <div className="signup__error">
                <MdErrorOutline />
                <p>{errors.email}</p>
              </div>
            )}

            {/* <label htmlFor="username">
              Tên đăng nhập
            </label>
            <div className={"signup-form__input " + (errors.username && "signup-form__input-error")}>
              <FaRegUser className="signup-form__input__icon" />
              <input
                type="text"
                placeholder="Tên đăng nhập"
                id="username"
                onChange={handleChange}
              />
            </div>
            {errors.username && (
              <div className="signup__error">
                <MdErrorOutline />
                <p>{errors.username}</p>
              </div>
            )} */}

            <label htmlFor="password">Mật khẩu</label>
            <div className={"signup-form__input " + (errors.password && "signup-form__input-error")}>
              <MdOutlineLock className="signup-form__input__icon" />
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Tối thiểu 8 ký tự"
                id="password"
                onChange={handleChange}
              />
              <button
                type="button"
                className="signup-form__input__icon"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <LuEye /> : <LuEyeOff />}
              </button>
            </div>

            {errors.password && (
              <div className="signup__error">
                <MdErrorOutline />
                <p>{errors.password}</p>
              </div>
            )}

            <label htmlFor="confirmPassword">Xác nhận mật khẩu</label>
            <div className={"signup-form__input " + (errors.confirmPassword && "signup-form__input-error")}>
              <RiRotateLockFill className="signup-form__input__icon" />
              <input
                type={showConfirmPassword ? "text" : "password"}
                placeholder="Tối thiểu 8 ký tự"
                id="confirmPassword"
                onChange={handleChange}
              />
              <button
                type="button"
                className="signup-form__input__icon"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              >
                {showConfirmPassword ? <LuEye /> : <LuEyeOff />}
              </button>
            </div>

            {errors.confirmPassword && (
              <div className="signup__error">
                <MdErrorOutline />
                <p>{errors.confirmPassword}</p>
              </div>
            )}

            {/* <label
              htmlFor="remember"
              className="signup-form__remember"
            >
              <input type="checkbox" id="remember" />
              <div>Ghi nhớ đăng nhập</div>
            </label>*/}

            <button type="submit" className="signup-form__btn">
              Đăng ký ngay <IoPersonAddSharp />
            </button>
          </form>

          {/* <div className="signup__text">
            Hoặc đăng ký bằng
          </div> */}

          {/* <button className="signup__google">
            <FcGoogle />
            Google
          </button> */}

          <div className="signup__signup">
            Đã có tài khoản?
            <Link to="/dang-nhap">Đăng nhập</Link>
          </div>
        </div>
      </div>
    </>
  )
}

export default Signup;