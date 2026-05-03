import { Rate } from "antd";
import "./ProductItem.scss";
import { Link } from "react-router-dom";

function ProductItem({ data }) {
  if (!data) return null;

  const displayPrice = data.salePrice > 0 ? data.salePrice : data.price;

  return (
    <Link to={`/san-pham/${data.id}`} className="product__item">
      <div className="product__item__image-box">
        <img
          src={data.imageUrl || "https://link-anh-demo.com/ao-thun.jpg"}
          alt={data.name}
          onError={(e) => {
            e.target.src = "https://via.placeholder.com/240x240?text=No+Image";
          }}
        />
      </div>

      <div className="product__item__info">
        <div className="product__item__price">
          {displayPrice.toLocaleString('vi-VN')}đ
        </div>

        <div className="product__item__shop">
          {data.brandName || "MenShop"}
        </div>

        <div className="product__item__title">
          {data.name}
        </div>

        <div className="product__item__bottom">
          <div className="product__item__rate-wrapper">
             <Rate
              disabled
              value={data.ratingAvg || 0}
              allowHalf
              style={{ fontSize: 12 }}
            />
          </div>

          <div className="product__item__sold">
            Đã bán {data.soldCount || 0}
          </div>
        </div>
      </div>
    </Link>
  );
}

export default ProductItem;