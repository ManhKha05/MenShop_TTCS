import "./Login.scss"
import { PiSealCheckBold } from "react-icons/pi";
import { MdOutlineLocalShipping } from "react-icons/md";
import { MdOutlineSupportAgent } from "react-icons/md";
import { MdShoppingBag } from "react-icons/md";
import { FaRegUser } from "react-icons/fa6";
import { MdOutlineLock } from "react-icons/md";
import { LuEye, LuEyeOff } from "react-icons/lu";
import { IoIosArrowRoundForward } from "react-icons/io";
import { FcGoogle } from "react-icons/fc";
import { MdErrorOutline } from "react-icons/md";
import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom"
import { post } from "../../../utils/request";
import { notification } from "antd";


function Login() {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });
  const [errors, setErrors] = useState({});
  const [searchParams] = useSearchParams();

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.id]: e.target.value
    }));
  }

  const validate = () => {
    const newErrors = {};
    if (!formData.email.trim()) newErrors.email = "Vui lòng nhập email";
    if (!formData.password) newErrors.password = "Vui lòng nhập mật khẩu";
    else if (formData.password.length < 8) {
      newErrors.password = "Mật khẩu phải có ít nhất 8 ký tự";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  useEffect(() => {
    const error = searchParams.get("error");

    if (error === "account_locked") {
      notification.error({
        title: "Tài khoản đã bị khóa",
        description: "Vui lòng liên hệ quản trị viên để hỗ trợ!",
      });
      setErrors({ fail: "Tài khoản đã bị khóa" });
    }
  }, [searchParams]);

  const handleLogin = (e) => {
    e.preventDefault();

    if (!validate()) return;
    console.log(formData);

    const fetchApi = async () => {
      try {
        const res = await post("auth/login", formData);

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message || "Đăng nhập thất bại");
        }

        const data = await res.json();

        console.log(data);
        localStorage.setItem("userId", data.userId);
        localStorage.setItem("token", data.token);
        localStorage.setItem("fullname", data.fullname);
        localStorage.setItem("email", data.email);
        localStorage.setItem("avatar", data.avatar);
        localStorage.setItem("shopId", data.shopId);
        localStorage.setItem("roles", JSON.stringify(data.roles));


        if (data.roles.includes("ROLE_ADMIN")) {
          navigate("/admin");
        } else if (data.roles.includes("ROLE_SHOP")) {
          navigate("/shop");
        } else {
          navigate("/"); // customer
        }
      } catch (error) {
        console.error(error);
        setErrors({ fail: error.message });
      }
    }
    fetchApi();
  }

  return (
    <>
      <div className="login">
        <div className="login__left">
          <div className="login__left__logo">
            {/* <MdShoppingBag />
            Chợ Số */}
          </div>
          <div className="login__left__content">
            <h1 className="login__left__title">
              Chào mừng bạn <br /> quay trở lại với Chợ Số
            </h1>
            <p className="login__left__desc">
              Khám phá thế giới mua sắm đỉnh cao, mua sắm an toàn và nhận nhiều ưu đãi độc quyền dành riêng cho bạn
            </p>
          </div>
          <div className="login__left__footer">
            <div className="login__left__footer-item">
              <PiSealCheckBold />
              <span>Hàng chính hãng</span>
            </div>
            <div className="login__left__footer-item">
              <MdOutlineLocalShipping />
              <span>Giao hàng nhanh</span>
            </div>
            <div className="login__left__footer-item">
              <MdOutlineSupportAgent />
              <span>Hỗ trợ 24/7</span>
            </div>
          </div>
        </div>

        <div className="login__right">
          <div className="login__title">
            Đăng nhập
          </div>
          <div className="login__desc">
            Vui lòng nhập thông tin để truy cập
          </div>

          {errors.fail && (
            <div className="login__error">
              <MdErrorOutline />
              <p>{errors.fail}</p>
            </div>
          )}


          <form className="login-form" onSubmit={handleLogin}>
            <label htmlFor="email">
              Email
            </label>
            <div className={"login-form__input " + (errors.email && "login-form__input-blank")} >
              <FaRegUser className="login-form__input__icon" />
              <input
                type="text"
                placeholder="Email"
                id="email"
                onChange={handleChange}
              />
            </div>

            {errors.email && (
              <div className="login__blank">
                <MdErrorOutline />
                <p>{errors.email}</p>
              </div>
            )}

            <div className="login-form__row">
              <label htmlFor="password">Mật khẩu</label>
              <Link to='/quen-mat-khau' className="login-form__forgot">Quên mật khẩu?</Link>
            </div>

            <div className={"login-form__input " + (errors.password && "login-form__input-blank")}>
              <MdOutlineLock className="login-form__input__icon" />
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Mật khẩu"
                id="password"
                onChange={handleChange}
              />
              <button
                type="button"
                className="login-form__input__icon"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <LuEye /> : <LuEyeOff />}
              </button>
            </div>

            {errors.password && (
              <div className="login__blank">
                <MdErrorOutline />
                <p>{errors.password}</p>
              </div>
            )}

            {/* <label
              htmlFor="remember"
              className="login-form__remember"
            >
              <input type="checkbox" id="remember" />
              <div>Ghi nhớ đăng nhập</div>
            </label> */}

            <button type="submit" className="login-form__btn">
              Đăng nhập <IoIosArrowRoundForward />
            </button>
          </form>

          <div className="login__text">
            Hoặc đăng nhập bằng
          </div>

          <button
            className="login__google"
            type="button"
            onClick={() => {
              window.location.href = "http://localhost:8080/oauth2/authorization/google";
            }}
          >
            <FcGoogle />
            Google
          </button>

          <div className="login__signup">
            Chưa có tài khoản?
            <Link to="/dang-ky">Đăng ký ngay</Link>
          </div>
        </div>
      </div>
    </>
  )
}

export default Login;