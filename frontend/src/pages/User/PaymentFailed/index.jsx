import "./PaymentFailed.scss";
import { MdOutlineCancel } from "react-icons/md";
import { useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";

function PaymentFailed() {
  const location = useLocation();
  const navigate = useNavigate();

  const paymentResult = location.state?.paymentResult;

  useEffect(() => {
    if (!paymentResult) {
      // vẫn cho vào trang, không bắt buộc phải có state
      // nên không redirect
    }
  }, [paymentResult]);

  return (
    <div className="payment-failed">
      <div className="container">
        <div className="payment-failed__icon">
          <MdOutlineCancel />
        </div>

        <div className="payment-failed__title">
          Thanh toán thất bại!
        </div>

        <div className="payment-failed__subtitle">
          Giao dịch của bạn chưa hoàn tất. Vui lòng thử lại hoặc chọn phương thức thanh toán khác.
        </div>

        <div className="payment-failed__info">
          <div className="payment-failed__item">
            <div className="payment-failed__item__title">
              Mã đơn hàng
            </div>
            <div className="payment-failed__item__content payment-failed__item__code">
              {paymentResult?.orderCode ? `#${paymentResult.orderCode}` : "--"}
            </div>
          </div>

          <div className="payment-failed__item">
            <div className="payment-failed__item__title">
              Mã phản hồi
            </div>
            <div className="payment-failed__item__content">
              {paymentResult?.responseCode || "--"}
            </div>
          </div>

          <div className="payment-failed__item">
            <div className="payment-failed__item__title">
              Thông báo
            </div>
            <div className="payment-failed__item__content">
              {paymentResult?.message || "Thanh toán chưa thành công"}
            </div>
          </div>
        </div>

        <div className="payment-failed__actions">
          <button
            className="payment-failed__btn payment-failed__btn-retry"
            onClick={() => navigate("/thanh-toan")}
          >
            Thử lại
          </button>

          <button
            className="payment-failed__btn payment-failed__btn-home"
            onClick={() => navigate("/")}
          >
            Về trang chủ
          </button>
        </div>
      </div>
    </div>
  );
}

export default PaymentFailed;