import "./OrderSuccess.scss";
import { FaRegCircleCheck } from "react-icons/fa6";
import { useLocation, useNavigate } from "react-router-dom";
import { formatPrice } from "../../../utils/price";

function OrderSuccess() {
  const location = useLocation();
  const navigate = useNavigate();

  const order = location.state?.order;

  return (
    <>
      <div className="order-success">
        <div className="order-success__icon">
          <FaRegCircleCheck />
        </div>
        <div className="order-success__title">
          Mua hàng thành công!
        </div>
        <div className="order-success__subtitle">
          Cảm ơn bạn đã tin tưởng mua sắm tại Chợ Số
        </div>

        <div className="order-success__info">
          {order.code && (
            <div className="order-success__item">
              <div className="order-success__item__title">
                Mã hóa đơn
              </div>
              <div className="order-success__item__content order-success__item__code ">
                #{order.code}
              </div>
            </div>
          )}

          <div className="order-success__item">
            <div className="order-success__item__title">
              Tổng thanh toán
            </div>
            <div className="order-success__item__content  ">
              {formatPrice(order.finalTotal)}
            </div>
          </div>
        </div>

        <div className="order-success__actions">
          <button
            className="order-success__btn order-success__btn-follow"
            onClick={() => navigate(`/tai-khoan/don-mua`)}
          >
            Đơn mua
          </button>

          <button
            className="order-success__btn order-success__btn-more"
            onClick={() => navigate("/")}
          >
            Tiếp tục mua sắm
          </button>
        </div>

      </div>
    </>
  )
}

export default OrderSuccess;