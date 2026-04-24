import "./ProductDetail.scss";
import { IoIosCheckmarkCircle } from "react-icons/io";
import { FaTimesCircle } from "react-icons/fa";
import { MdLock } from "react-icons/md";
import { HiAdjustmentsHorizontal } from "react-icons/hi2";
import { MdDescription } from "react-icons/md";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { get, put } from "../../utils/request";
import { Tag, notification } from "antd";

function ProductDetail({ role }) {
  const { id } = useParams();
  const [reload, setReload] = useState(false);
  const [product, setProduct] = useState({});
  const [thumbnail, setThumbnail] = useState("");

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const res = await get(`${role === "ADMIN" ? "admin" : "shop"}/products/${id}`);
        const data = await res.json();

        setProduct(data);

        if (data.images && data.images.length > 0) {
          setThumbnail(data.images[0]);
        }

      } catch (err) {
        console.error(err);
      }
    };

    fetchProduct();
  }, [id, role, reload]);
  console.log(product);

  const colors = [...new Set(product?.variants?.map(v => v.color))];
  const sizes = [...new Set(product?.variants?.map(v => v.size))];

  const renderStatus = (status) => {
    switch (status) {
      case "ACTIVE":
        return <Tag className="status-tag status-tag--active">Đang bán</Tag>;
      case "PENDING":
        return <Tag className="status-tag status-tag--pending">Chờ duyệt</Tag>;
      case "INACTIVE":
        return <Tag className="status-tag status-tag--inactive">Tạm ẩn</Tag>;
      case "REJECTED":
        return <Tag className="status-tag status-tag--rejected">Bị từ chối</Tag>;
      default:
        return <Tag className="status-tag">{status}</Tag>;
    }
  };

  const updateStatus = async (action) => {
    try {
      await put(`${role === "ADMIN" ? "admin" : "shop"}/products/${id}/${action}`);

      let msg = "";
      switch (action) {
        case "approve":
          msg = "Duyệt sản phẩm thành công";
          break;
        case "reject":
          msg = "Từ chối sản phẩm";
          break;
        case "inactive":
          msg = "Đã ẩn sản phẩm";
          break;
        default:
          msg = "Cập nhật thành công";
      }

      notification.success({
        message: "Thành công",
        description: msg,
        showProgress: true
      });

      setReload(!reload);

    } catch (err) {
      notification.error({
        message: "Lỗi",
        description: err.message,
      });
    }
  };

  return (
    <>
      <div className="product-detail">
        <div className="product-detail__top">
          <div className="product-detail__top__left">
            <div className="product-detail__title">
              Sản phẩm: {product.name}
            </div>
            <div>
              {renderStatus(product.status)}
            </div>
          </div>

          <div className="product-detail__action">

            {/* ================= ADMIN ================= */}
            {role === "ADMIN" && (
              <>
                {product.status === "PENDING" && (
                  <>
                    <button
                      className="product-detail__btn product-detail__btn-one"
                      onClick={() => updateStatus("approve")}
                    >
                      <IoIosCheckmarkCircle />
                      Duyệt
                    </button>

                    <button
                      className="product-detail__btn product-detail__btn-two"
                      onClick={() => updateStatus("reject")}
                    >
                      <FaTimesCircle />
                      Từ chối
                    </button>
                  </>
                )}

                {/* {product.status === "ACTIVE" && (
                  <button
                    className="product-detail__btn product-detail__btn-three"
                    onClick={() => updateStatus("inactive")}
                  >
                    <MdLock />
                    Khóa
                  </button>
                )}

                {product.status === "INACTIVE" && (
                  <button
                    className="product-detail__btn product-detail__btn-one"
                    onClick={() => updateStatus("approve")}
                  >
                    <IoIosCheckmarkCircle />
                    Mở bán
                  </button>
                )} */}
              </>
            )}

            {/* ================= SHOP ================= */}
            {role === "SHOP" && (
              <>
                {product.status === "ACTIVE" && (
                  <button
                    className="product-detail__btn product-detail__btn-three"
                    onClick={() => updateStatus("inactive")}
                  >
                    <MdLock />
                    Ẩn sản phẩm
                  </button>
                )}

                {product.status === "INACTIVE" && (
                  <button
                    className="product-detail__btn product-detail__btn-one"
                    onClick={() => updateStatus("active")}
                  >
                    <IoIosCheckmarkCircle />
                    Mở bán
                  </button>
                )}
              </>
            )}

          </div>
        </div>

        <div className="product-detail__body">
          <div className="product-detail__library">
            <img src={thumbnail} className="product-detail__thumbnail" />

            <div className="product-detail__images">
              {product.images?.map((img, index) => (
                <img
                  key={index}
                  src={img}
                  onClick={() => setThumbnail(img)}
                  className={`product-detail__image ${thumbnail === img ? "selected" : ""}`}
                />
              ))}
            </div>
          </div>

          <div className="product-detail__content">
            <div className="product-detail__row">
              <div className="product-detail__price">
                <div className="product-detail__price__title">
                  GIÁ BÁN
                </div>
                <div className="product-detail__price__value">
                  {(product.salePrice || product.price)?.toLocaleString()}đ
                </div>
              </div>

              <Link to={`/admin/shops/${product.shopId}`} className="product-detail__shop">
                <div className="product-detail__shop__title">
                  CỬA HÀNG
                </div>
                <div className="product-detail__shop__info">
                  <img src={product.shopImg} alt="" className="product-detail__shop__image" />
                  <div className="product-detail__shop__name">
                    {product.shopName}
                  </div>
                </div>
              </Link>
            </div>

            <div className="product-detail__info">
              <div className="product-detail__desc__title">
                <MdDescription />
                Mô tả sản phẩm
              </div>

              <div className="product-detail__desc__content">
                {product.description}
              </div>

              <div className="product-detail__info__row">
                <div className="product-detail__info__row__item">
                  <div className="product-detail__info__name">
                    Tên thương hiệu
                  </div>
                  <div className="product-detail__info__value">
                    {product.brandName || ""}
                  </div>
                </div>
                <div className="product-detail__info__row__item">
                  <div className="product-detail__info__name">
                    PHÂN LOẠI MÀU SẮC
                  </div>
                  <div className="product-detail__info__list">
                    {colors.map((c, i) => (
                      <div key={i} className="product-detail__info__item">{c}</div>
                    ))}
                  </div>
                </div>

                <div className="product-detail__info__row__item">
                  <div className="product-detail__info__name">
                    KÍCH CỠ HIỆN CÓ
                  </div>
                  <div className="product-detail__info__list">
                    {sizes.map((s, i) => (
                      <div key={i} className="product-detail__info__item">{s}</div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="product-detail__details">
                <div className="product-detail__info__name">
                  CHI TIẾT SẢN PHẨM
                </div>

                {Object.entries(product.attributesJson || {}).map(([key, value], index) => (
                  <div className="product-detail__detail" key={index}>
                    <div className="product-detail__detail__name">
                      {key}:
                    </div>
                    <div className="product-detail__detail__value">
                      {value}
                    </div>
                  </div>
                ))}

              </div>
            </div>
          </div>
        </div>

      </div>
    </>
  )
}

export default ProductDetail;