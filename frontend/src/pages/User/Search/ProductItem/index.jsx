import { Rate } from "antd";
import "./ProductItem.scss";
import { Link } from "react-router-dom";

function ProductItem() {
  return (
    <>
      <Link to='/san-pham/abc-pham-p13-p12' className="product__item">
        <img src="https://salt.tikicdn.com/cache/750x750/ts/product/5c/bb/4e/aa88077826bd39990b004aca36833c72.jpg.webp" alt="" className="product__item__image" />
        <div className="product__item__price">
          243.000 <sup>₫</sup>
        </div>
        <div className="product__item__shop">
          Manhkha Store
        </div>
        <div className="product__item__title">
          Chuột không dây Logitech B175 - Sđầu thu USB 2.4Ghz, pin 1 năm, nhỏ gọn, thiết kế thuận cả 2 tay, phù hợp PC/ Laptop - Hàng chính hãng
        </div>
        <div className="product__item__bottom">
          <Rate
            disabled
            defaultValue={3}
            size="small"
          />
          <div className="product__item__sold">
            Đã bán 25
          </div>
        </div>

      </Link>
    </>
  )
}

export default ProductItem;