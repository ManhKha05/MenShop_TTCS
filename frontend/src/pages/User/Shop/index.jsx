import "./Shop.scss";
import { IoIosStar } from "react-icons/io";
import { IoPersonAddSharp } from "react-icons/io5";
import { IoChatboxSharp } from "react-icons/io5";
import { BiSolidHot } from "react-icons/bi";
import { TiThList } from "react-icons/ti";
import { IoIosArrowDown } from "react-icons/io";
import { IoIosArrowBack, IoIosArrowForward } from "react-icons/io";
import { Col, Row } from "antd";
import { useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom"
import { get } from "../../../utils/request";
import { formatPrice } from "../../../utils/price";
import { connectSocket, disconnectSocket, subscribeSocket } from "../../../utils/socket";

function Shop() {
  const allProductsRef = useRef(null);
  const { id } = useParams();
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(20);
  const [sort, setSort] = useState("popular"); // popular | newest | best-selling
  const [priceOrder, setPriceOrder] = useState(""); // asc | desc
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [totalPages, setTotalPages] = useState(1);
  const [shop, setShop] = useState({});
  const [categories, setCategories] = useState([]);
  const [bestSellingProducts, setBestSellingProducts] = useState([]);
  const [products, setProducts] = useState([]);

  const fetchOneTime = async () => {
    try {
      const [shopRes, categoriesRes, bestSellingRes] = await Promise.all([
        get(`shops/${id}`),
        get(`shops/${id}/categories`),
        get(`shops/${id}/best-selling-products`)
      ])

      const shopData = await shopRes.json();
      const categoriesData = await categoriesRes.json();
      const bestSellingData = await bestSellingRes.json();

      setShop(shopData);
      setCategories(categoriesData);
      setBestSellingProducts(bestSellingData);
    } catch (error) {
      console.log(error);
    }
  }

  useEffect(() => {
    fetchOneTime();
  }, [id])

  const fetchProducts = async () => {
    try {
      const res = await get(`shops/${id}/products`, {
        page: page - 1,
        size,
        sort,
        priceOrder,
        category: selectedCategory || null
      });

      const data = await res.json();

      setProducts(data.content);
      setTotalPages(Math.ceil(data.totalElements / size));

    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [id, page, size, sort, priceOrder, selectedCategory]);

  useEffect(() => {
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket(`/topic/shop-products/${id}`, (data) => {
        if (!data) return;

        if (
          data.type === "PRODUCT_UPDATED"
        ) {
          fetchProducts();
        }
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [id, products]);

  console.log(bestSellingProducts);

  return (
    <>
      <div className="shop">
        <div className="container">
          <div className="shop__top">
            <div className="shop__content">
              <img src={shop.logo} alt="" className="shop__image" />
              <div className="shop__info">
                <div className="shop__info__top">
                  <div className="shop__info__name">
                    {shop.name}
                  </div>
                  {/* <div className="shop__info__rating">
                    <IoIosStar />
                    4.9/5.0
                  </div> */}
                </div>

                <div className="shop__info__bottom">
                  <div className="shop__info__item">
                    {shop.totalProducts}
                    <span>Sản phẩm</span>
                  </div>
                  <div className="shop__info__item">
                    {shop.joinedDays} ngày
                    <span>Tham gia</span>
                  </div>
                  {/* <div className="shop__info__item">
                    98%
                    <span>Phản hồi Chat</span>
                  </div> */}
                </div>
              </div>
            </div>

            {/* <div className="shop__actions">
              <button className="shop__action shop__action--primary">
                <IoPersonAddSharp />
                Theo dõi
              </button>
              <button className="shop__action shop__action--default">
                <IoChatboxSharp />
                Chat ngay
              </button>
            </div> */}
          </div>


          <div className="shop__body">
            <div className="shop__nav">
              <button className="shop__nav__item">
                Trang chủ
              </button>
              <button className="shop__nav__item" onClick={() => allProductsRef.current.scrollIntoView({ behavior: "smooth" })}>
                Tất cả sản phẩm
              </button>
            </div>

            <div className="shop__top-sell">
              <div className="shop__top-sell__title">
                <BiSolidHot />
                Sản phẩm bán chạy
              </div>

              <div className="shop__top-sell__products">
                {bestSellingProducts.map(p => (
                  <Link to={`/san-pham/${p.id}`} className="product">
                    <img src={p.image} alt="" className="product__image" />
                    <div className="product__name">
                      {p.name}
                    </div>
                    <div className="product__row">
                      <div className="product__price">
                        {formatPrice(p.salePrice || p.price)}
                      </div>
                      <div className="product__sold">
                        Đã bán {p.sold}
                      </div>
                    </div>
                  </Link>
                ))}

              </div>
            </div>

            <div className="product-page" ref={allProductsRef}>
              <div className="product-page__title">
                Tất cả sản phẩm
              </div>

              <div className="product-page__layout">
                <div className="product-page__category">
                  <div className="product-page__category__title">
                    <TiThList />
                    DANH MỤC
                  </div>

                  <ul className="product-page__category__list">
                    <li
                      className={`product-page__category__item ${selectedCategory === null ? "active" : ""}`}
                      onClick={() => setSelectedCategory(null)}
                    >
                      Tất cả
                    </li>
                    {categories.map(it => (
                      <li
                        key={it.id}
                        className={`product-page__category__item ${selectedCategory === it.id ? "active" : ""}`}
                        onClick={() => setSelectedCategory(it.id)}
                      >
                        {it.name}
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="product-page__content">
                  <div className="product-page__filter">
                    <span>Sắp xếp theo</span>

                    <button
                      className={`product-page__filter-btn ${sort === "popular" ? "active" : ""}`}
                      onClick={() => {
                        setSort("popular");
                        setPriceOrder(null);
                      }}
                    >
                      Phổ biến
                    </button>
                    <button
                      className={`product-page__filter-btn ${sort === "newest" ? "active" : ""}`}
                      onClick={() => {
                        setSort("newest");
                        setPriceOrder(null);
                      }}
                    >
                      Mới nhất
                    </button>
                    <button
                      className={`product-page__filter-btn ${sort === "best-selling" ? "active" : ""}`}
                      onClick={() => {
                        setSort("best-selling");
                        setPriceOrder(null);
                      }}
                    >
                      Bán chạy
                    </button>

                    <div className="product-page__filter-price">
                      <button className={`product-page__filter-btn product-page__filter-btn-price ${priceOrder && "select"}`}>
                        Giá
                        {priceOrder === "asc" ? ": Thấp đến cao" : (priceOrder === "desc" ? ": Cao đến Thấp" : "")}
                        <IoIosArrowDown />
                      </button>

                      <div className="product-page__filter-dropdown">
                        <div
                          className="product-page__filter-dropdown__item"
                          onClick={() => {
                            setPriceOrder("asc");
                            setSort(null);
                          }}
                        >
                          Giá: Thấp đến Cao
                        </div>
                        <div
                          className="product-page__filter-dropdown__item"
                          onClick={() => {
                            setPriceOrder("desc");
                            setSort(null);
                          }}
                        >
                          Giá: Cao đến Thấp
                        </div>
                      </div>
                    </div>
                  </div>

                  <div className="product-page__grid">

                    {products.map(p => (
                      <Link to={`/san-pham/${p.id}`} className="product" key={p.id}>
                        <img src={p.image} alt="" className="product__image" />
                        <div className="product__name">
                          {p.name}
                        </div>

                        <div className="product__row">
                          <div className="product__price">
                            {formatPrice(p.salePrice || p.price)}
                          </div>
                          {p.salePrice && (
                            <div className="product__discount">
                              {Math.floor((1 - p.salePrice / p.price) * 100)}%
                            </div>
                          )}
                        </div>

                        <div className="product__row">
                          <div className="product__rating">
                            <IoIosStar />
                            {Math.round(p.rating, 1)}
                          </div>
                          <div className="product__sold">
                            Đã bán {p.sold}
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>

                  <div className="product-page__pagination">
                    <button
                      className="product-page__pagination-btn"
                      disabled={page === 1}
                      onClick={() => setPage(prev => prev - 1)}
                    >
                      <IoIosArrowBack />
                    </button>

                    {[...Array(totalPages)].map((_, index) => (
                      <button
                        key={index}
                        className={`product-page__pagination-btn ${page === index + 1 ? "current" : ""}`}
                        onClick={() => setPage(index + 1)}
                      >
                        {index + 1}
                      </button>
                    ))}

                    <button
                      className="product-page__pagination-btn"
                      disabled={page === totalPages}
                      onClick={() => setPage(prev => prev + 1)}
                    >
                      <IoIosArrowForward />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Shop;