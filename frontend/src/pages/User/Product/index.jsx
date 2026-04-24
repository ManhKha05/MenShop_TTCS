import { Link, useNavigate, useParams } from "react-router-dom";
import { Breadcrumb, Empty, Rate, Tabs, notification } from "antd"
import { MdAddShoppingCart } from "react-icons/md";
import { MdOutlineChat } from "react-icons/md";
import { BsShopWindow } from "react-icons/bs";
import { TiStarFullOutline } from "react-icons/ti";
import { IoStar } from "react-icons/io5";
import "./Product.scss"
import { useContext, useEffect, useState } from "react";
import { get, post } from "../../../utils/request";
import { formatPrice } from "../../../utils/price";
import { CartContext } from "../../../components/CartContext";
import { connectSocket, disconnectSocket, subscribeSocket } from "../../../utils/socket";
import { formatDateTime } from "../../../utils/date"

function Product() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [thumbnail, setThumbnail] = useState("https://down-vn.img.susercontent.com/file/vn-11134207-7ras8-m3bufh4u6qw894.webp")
  const [color, setColor] = useState();
  const [size, setSize] = useState();
  const [product, setProduct] = useState({});
  const [quantity, setQuantity] = useState(0);

  const [pageReview, setPageReview] = useState(0);
  const [pageReviewSize, setPageReviewSize] = useState(10);
  const [totalReview, setTotalReview] = useState(0);
  const [reviews, setReviews] = useState([]);

  const { addVariant, token } = useContext(CartContext);

  const fetchProduct = async () => {
    try {
      const [productRes] = await Promise.all([
        get(`products/${id}`)
      ])

      const productData = await productRes.json();

      setProduct(productData);

      if (productData.images?.length > 0) {
        setThumbnail(productData.images[0]);
      }

    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    fetchProduct();
  }, [id])

  const fetchReviews = async () => {
    try {
      const res = await get(`reviews/product/${id}`, {
        page: pageReview,
        size: pageReviewSize
      });

      const data = await res.json();
      setReviews(data.content || []);
      setTotalReview(data.totalElements);
    } catch (error) {
      console.error("Lỗi lấy đánh giá:", error);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [id, pageReview]);

  console.log("reviews", reviews);

  useEffect(() => {
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket("/topic/products", (data) => {
        if (!data) return;

        if (
          data.type === "PRODUCT_UPDATED" &&
          Number(data.productId) === Number(id)
        ) {
          fetchProduct();
        }
        if (
          data.type === "PRODUCT_REVIEW" &&
          Number(data.productId) === Number(id)
        ) {
          fetchReviews();
        }
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [id, product, reviews]);

  const colors = [...new Set(product.variants?.map(v => v.color))];
  const sizes = [...new Set(product.variants?.map(v => v.size))];

  const selectedVariant = product.variants?.find(
    v => v.color === color && v.size === size
  );
  const maxStock = selectedVariant?.stock || 0;

  const handleChangeQuantity = (value) => {
    if (value < 1) value = 0;
    if (value > maxStock) value = maxStock;
    setQuantity(value);
  };

  const handleAddToCart = async () => {
    if (!token) {
      notification.warning({
        message: "Vui lòng đăng nhập để tiếp tục",
        placement: "topRight"
      });
      return;
    }

    if (!selectedVariant) {
      notification.error({
        title: "Thất bại",
        description: "Vui lòng chọn màu sắc và kích cỡ!",
        showProgress: true
      })
      return;
    }
    if (quantity < 1) {
      notification.error({
        title: "Thất bại",
        description: "Số lượng phải lớn hơn 0!",
        showProgress: true
      })
      return;
    }

    try {
      const params = new URLSearchParams({
        variantId: selectedVariant.id,
        quantity: quantity,
      });

      const response = await post(`cart-items?${params.toString()}`);

      if (!response.ok) {
        throw new Error("Thêm vào giỏ hàng thất bại");
      }

      notification.success({
        title: "Thành công",
        description: "Đã thêm vào giỏ hàng!",
        showProgress: true
      })

      addVariant(selectedVariant.id);


    } catch (error) {
      console.error(error);
      notification.error({
        title: "Thất bại",
        description: "Thêm vào giỏ hàng thất bại!",
        showProgress: true
      })
    }
  };

  const handleBuyNow = async () => {
    if (!token) {
      notification.warning({
        message: "Vui lòng đăng nhập để tiếp tục",
        placement: "topRight"
      });
      return;
    }

    if (!selectedVariant) {
      notification.error({
        message: "Thất bại",
        description: "Vui lòng chọn màu sắc và kích cỡ!"
      });
      return;
    }

    if (quantity < 1) {
      notification.error({
        message: "Thất bại",
        description: "Số lượng phải lớn hơn 0!"
      });
      return;
    }

    if (quantity > maxStock) {
      notification.error({
        message: "Thất bại",
        description: "Số lượng vượt quá tồn kho!"
      });
      return;
    }

    const buyNowItem = [
      {
        type: "buy_now",
        variantId: selectedVariant.id,
        productId: product.id,
        productName: product.name,
        imageUrl: product.images?.[0] || thumbnail,
        color: selectedVariant.color,
        size: selectedVariant.size,
        quantity: Number(quantity),
        displayPrice: product.flashPrice || product.salePrice || product.price,
      }
    ];

    localStorage.setItem("checkoutItems", JSON.stringify(buyNowItem));
    navigate("/thanh-toan");
  };

  console.log(product);

  return (
    <>
      <div className="product">
        <div className="container">
          {/* <Breadcrumb
            items={[
              {
                title: <Link to="/">Trang chủ</Link>,
              },
              {
                title: <Link to="/thiet-bi-so-phu-kien-so">Thời trang nam</Link>,
              },
              {
                title: 'Áo Sơ Mi Nam Linen Preminum'
              },
            ]}
          /> */}
          <div className="product__main">
            <div className="product-gallery">
              <img src={thumbnail} className="product-gallery__main" alt="" />

              <div className="product-gallery__thumbnails">
                {product.images?.map((img, index) => (
                  <img
                    key={index}
                    className={`product-gallery__thumbnail ${img === thumbnail && "active"}`}
                    src={img}
                    onClick={() => setThumbnail(img)}
                  />
                ))}
              </div>
            </div>

            <div className="product-info">
              <div className="product-info__title">
                {product.name}
              </div>
              <div className="product-info__static">
                <div className="product-info__rate">
                  <IoStar />
                  <span>{product.ratingAvg}</span>
                </div>
                <div className="product-info__rate-number">
                  {reviews.length} Đánh giá
                </div>
                <div className="product-info__sold">
                  Đã bán {product.soldCount}
                </div>
              </div>

              <div className="product-info__price">
                <div className="product-info__current-price">
                  {formatPrice(product.flashPrice || product.salePrice || product.price)}
                </div>

                {(product.flashPrice || product.salePrice) && (
                  <>
                    <div className="product-info__old-price">
                      {formatPrice(product.price)}
                    </div>

                    <div className="product-info__discount">
                      -{Math.round((1 - (product.flashPrice || product.salePrice) / product.price) * 100)}%
                    </div>
                  </>
                )}
              </div>

              <div className="product-options">
                <div className="product-options__title">
                  Màu sắc
                </div>
                <div className="product-options__list">
                  {colors.map(c => (
                    <div
                      key={c}
                      className={`product-options__item ${color === c ? "active" : ""}`}
                      onClick={() => setColor(c)}
                    >
                      {c}
                    </div>
                  ))}
                </div>
              </div>

              <div className="product-options">
                <div className="product-options__title">
                  Kích cỡ
                </div>
                <div className="product-options__list">
                  {sizes.map(s => (
                    <div
                      key={s}
                      className={`product-options__item ${size === s ? "active" : ""}`}
                      onClick={() => setSize(s)}
                    >
                      {s}
                    </div>
                  ))}
                </div>
              </div>

              <div className="product-quantity">
                <div className="product-options__title">
                  Số lượng
                </div>
                <div className="product-quantity__content">
                  <button className="product-quantity__minus" onClick={() => handleChangeQuantity(quantity - 1)}>
                    -
                  </button>
                  <input
                    type="number"
                    className="product-quantity__input"
                    value={quantity}
                    onChange={(e) => handleChangeQuantity(e.target.value)}
                  />
                  <button className="product-quantity__plus" onClick={() => handleChangeQuantity(quantity + 1)}>
                    +
                  </button>
                </div>
                <div className="product-quantity__available">
                  {maxStock} sản phẩm có sẵn
                </div>
              </div>

              <div className="product-actions">
                <button className="product__add-to-cart" onClick={handleAddToCart}>
                  <MdAddShoppingCart />
                  Thêm vào giỏ
                </button>
                <button className="product__buy-now" onClick={handleBuyNow}>
                  Mua ngay
                </button>
              </div>
            </div>

            <div className="product-shop">
              <div className="product-shop__header">
                <img
                  className="product-shop__image"
                  src={product.shopImg}
                  alt=""
                />
                <div className="product-shop__info">
                  <div className="product-shop__name">
                    {product.shopName}
                  </div>
                  <div className="product-shop__static">
                    {/* <IoStar className="product-shop__static-star" />
                    4.9/5 */}
                    {/* <GoDotFill className="product-shop__static-dot" />
                    3 năm trên nền tảng */}
                  </div>
                </div>
              </div>

              <div className="product-shop__body">
                <div className="product-shop__body__title">
                  THỜI GIAN PHẢN HỒI
                </div>
                <div className="product-shop__body__content">
                  Trong vài phút
                </div>
              </div>

              <div className="product-shop__footer">
                {/* <button className="product-shop__chat">
                  <MdOutlineChat />
                  CHAT NGAY
                </button> */}
                {/* <Link to={`/cua-hang/${product.shopId}`}> */}
                <button className="product-shop__view">
                  <Link to={`/cua-hang/${product.shopId}`}>
                    <BsShopWindow />
                    XEM SHOP
                  </Link>

                </button>
                {/* </Link> */}
              </div>
            </div>
          </div>

          <Tabs
            defaultActiveKey="1"
            items={[
              {
                key: '1',
                label: 'Mô tả sản phẩm',
                children: (
                  <div style={{ whiteSpace: "pre-line", fontSize: '16px' }}>
                    {product.description}
                  </div>
                ),
              },
              {
                key: '2',
                label: 'Thông tin chi tiết',
                children: (
                  <div className="product-detail">
                    {product.attributesJson &&
                      Object.entries(product.attributesJson).map(([key, value]) => (
                        <div className="product-detail__item" key={key}>
                          <div className="product-detail__item__title">
                            {key}
                          </div>
                          <div className="product-detail__item__content">
                            {value}
                          </div>
                        </div>
                      ))}
                  </div>
                ),
              },
              {
                key: '3',
                label: 'Đánh giá sản phẩm',
                children: (
                  <div className="product-reviews">
                    <div className="product-reviews">
                      {reviews.length === 0 ? (
                        <Empty description="Chưa có đánh giá nào" />
                      ) : (
                        reviews.map((item) => (
                          <div className="product-review" key={item.id}>
                            <img
                              className="product-review__image"
                              src={item.userAvatar || "https://via.placeholder.com/40"}
                              alt={item.userName}
                            />
                            <div className="product-review__body">
                              <div className="product-review__top">
                                <div className="product-review__name">{item.userName}</div>
                                <div className="product-review__time">
                                  {formatDateTime(item.createdAt)}
                                </div>
                              </div>

                              <Rate
                                allowHalf
                                disabled
                                value={Number(item.rating)}
                                size="small"
                                className="product-review__rate"
                              />

                              {item.images?.length > 0 && (
                                <div className="product-review__images">
                                  {item.images.map((img) => (
                                    <img
                                      key={img.id}
                                      src={img.imageUrl}
                                      alt=""
                                      className="product-review__thumb"
                                    />
                                  ))}
                                </div>
                              )}

                              <div className="product-review__content" style={{ whiteSpace: "pre-line" }}>
                                {item.content}
                              </div>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                ),
              },
            ]}
          />

          <div className="related-products">
            <div className="related-products__title">
              SẢN PHẨM TƯƠNG TỰ
            </div>
            <div className="related-products__body">
              <Link to={`/san-pham./1`} className="related-product">
                <img
                  src="https://down-vn.img.susercontent.com/file/vn-11134207-7r98o-lm7427ucy50ffd.webp"
                  alt=""
                  className="related-product__image"
                />
                <div className="related-product__main">
                  <div className="related-product__name">
                    Áo Sơ Mi Denim Nam Dekace Cao Cấp Vải Dày Co Giãn SMDE
                  </div>
                  <div className="related-product__rate">
                    <TiStarFullOutline />
                    4.9
                  </div>
                  <div className="related-product__row">
                    <div className="related-product__price">
                      230.300đ
                    </div>
                    <div className="related-product__sold">
                      Đã bán 85
                    </div>
                  </div>
                </div>
              </Link>
            </div>

            {/* <div style={{ textAlign: "center" }}>
              <button className="related-products__btn">
                Xem thêm
              </button>
            </div> */}
          </div>
        </div>
      </div>
    </>
  )
}

export default Product;