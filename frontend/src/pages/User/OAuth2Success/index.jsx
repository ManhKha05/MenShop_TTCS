import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../../utils/request";

function OAuth2Success() {
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const params = new URLSearchParams(window.location.search);
        const token = params.get("token");

        if (!token) {
          navigate("/dang-nhap");
          return;
        }

        localStorage.setItem("token", token);

        const res = await get("auth/me");

        if (!res.ok) {
          console.log("Lỗi")
          throw new Error("Không lấy được thông tin người dùng");
        }

        const data = await res.json();

        localStorage.setItem("userId", data.userId || "")
        localStorage.setItem("fullname", data.fullname || "");
        localStorage.setItem("email", data.email || "");
        localStorage.setItem("avatar", data.avatar || "");
        localStorage.setItem("shopId", data.shopId || "");
        localStorage.setItem("roles", JSON.stringify(data.roles || []));

        if (data.roles?.includes("ROLE_ADMIN")) {
          navigate("/admin");
        } else if (data.roles?.includes("ROLE_SHOP")) {
          navigate("/shop");
        } else {
          navigate("/");
        }
      } catch (error) {
        console.error(error);
        localStorage.removeItem("token");
        navigate("/dang-nhap");
      }
    };

    fetchProfile();
  }, [navigate]);

  return <div>Đang đăng nhập bằng Google...</div>;
}

export default OAuth2Success;