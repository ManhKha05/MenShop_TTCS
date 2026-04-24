import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { get } from "../../../utils/request";

function VnPayReturn() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchResult = async () => {
      try {
        const res = await get(`vnpay/return${location.search}`);
        const data = await res.json();

        if (data.success) {
          navigate("/dat-hang-thanh-cong", {
            state: {
              order: {
                id: data.orderId,
                code: data.orderCode,
                finalTotal: data.finalTotal,
              },
            },
          });
        } else {
          navigate("/thanh-toan-that-bai", {
            state: { paymentResult: data },
          });
        }
      } catch (error) {
        navigate("/thanh-toan-that-bai");
      }
    };

    fetchResult();
  }, [location.search, navigate]);

  return <div>Đang xử lý kết quả thanh toán...</div>;
}

export default VnPayReturn;