import "./Search.scss";
import { IoIosArrowDown } from "react-icons/io";
import { Link, useSearchParams } from "react-router-dom";
import { Breadcrumb, Pagination, Spin } from "antd";
import { useState, useEffect } from "react";
import ModalFilter from "./ModalFilter";
import ProductItem from "./ProductItem";
import { get } from "../../../utils/request";

function Search() {
  const [searchParams] = useSearchParams();
  const keyword = searchParams.get("keyword") || "";

  const [showSort, setShowSort] = useState(false);
  const [sort, setSort] = useState({ value: "default", title: "Phổ biến" });

  const [filters, setFilters] = useState({ minStar: null, minPrice: null, maxPrice: null });

  const [originalProducts, setOriginalProducts] = useState([]);
  const [displayProducts, setDisplayProducts] = useState([]);

  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 10;

  useEffect(() => {
    if (!keyword) return;

    const fetchProducts = async () => {
      setLoading(true);
      try {
        const response = await get(`search?keyword=${keyword}&page=0&size=60`);

        if (!response.ok) {
           console.log("Không tìm thấy dữ liệu hoặc lỗi server");
           setOriginalProducts([]);
           setDisplayProducts([]);
           return;
        }

        const data = await response.json();

        setOriginalProducts(data.content);
        setDisplayProducts(data.content);

        setCurrentPage(1);
        setSort({ value: "default", title: "Phổ biến" });
        setFilters({ minStar: null, minPrice: null, maxPrice: null });

      } catch (error) {
        console.error("Lỗi khi fetch tìm kiếm:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [keyword]);

  useEffect(() => {
    if (originalProducts.length === 0) {
      setDisplayProducts([]);
      return;
    }

    let processedList = [...originalProducts];
    const getActualPrice = (item) => item.salePrice ? item.salePrice : item.price;

    if (filters.minPrice !== null) {
      processedList = processedList.filter(item => getActualPrice(item) >= filters.minPrice);
    }
    if (filters.maxPrice !== null) {
      processedList = processedList.filter(item => getActualPrice(item) <= filters.maxPrice);
    }
    if (filters.minStar !== null) {
      processedList = processedList.filter(item => (item.ratingAvg || 0) >= filters.minStar);
    }

    if (sort.value === "price_asc") {
      processedList.sort((a, b) => getActualPrice(a) - getActualPrice(b));
    } else if (sort.value === "price_desc") {
      processedList.sort((a, b) => getActualPrice(b) - getActualPrice(a));
    } else if (sort.value === "top_seller") {
      processedList.sort((a, b) => (b.soldCount || 0) - (a.soldCount || 0));
    }

    setDisplayProducts(processedList);
    setCurrentPage(1);

  }, [sort, filters, originalProducts]);

  const currentProducts = displayProducts.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

  const sortOptions = [
    { value: "default", title: "Phổ biến" },
    { value: "top_seller", title: "Bán chạy" },
    { value: "price_asc", title: "Giá thấp đến cao" },
    { value: "price_desc", title: "Giá cao đến thấp" },
  ];

  return (
    <>
      <div className="search">
        <div className="container">
          <Breadcrumb
            items={[
              {
                title: <Link to="/">Trang chủ</Link>,
              },
              {
                title: `Kết quả tìm kiếm "${keyword}"`
              },
            ]}
          />

          <div className="search__top">
            <div className="search__result">
              {loading ? (
                <span>Đang tìm kiếm...</span>
              ) : (
                <span>Tìm thấy {displayProducts.length} sản phẩm liên quan đến "{keyword}"</span>
              )}
            </div>
            <div className="search__filter">
              <div className="search__filter__sort">
                <span>Sắp xếp</span>
                <button
                  className="search__filter__sort-selected"
                  onClick={() => setShowSort(!showSort)}
                >
                  {sort.title} <IoIosArrowDown />
                </button>
                {showSort && (
                  <div className="search__filter__sort__dropdown">
                    {sortOptions.map((item) => (
                      <div
                        key={item.value}
                        className="sort-item"
                        onClick={() => {
                          setSort(item);
                          setShowSort(false);
                        }}
                      >
                        {item.title}
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <span style={{ color: 'rgb(128, 128, 137)' }} >Bộ lọc</span>

              <ModalFilter onApplyFilter={(newFilters) => setFilters(newFilters)} />

            </div>
          </div>

        {loading ? (
          <div className="search__loading">
            <Spin size="large" tip="Đang tìm kiếm sản phẩm..." />
          </div>
        ) : currentProducts.length > 0 ? (
          <div className="product__list">
            {currentProducts.map((product) => (
              <ProductItem key={product.id} data={product} />
            ))}
          </div>
        ) : (
          <div className="search__empty">
            <div className="search__empty__content">
              <img
                src="https://cdn-icons-png.flaticon.com/512/6134/6134065.png"
                alt="not found"
                style={{ width: 120, marginBottom: 20, opacity: 0.6 }}
              />
              <h3>Rất tiếc, không tìm thấy sản phẩm liên quan đến "{keyword}"</h3>
              <p>Vui lòng thử lại với từ khóa khác hoặc kiểm tra lại bộ lọc.</p>
            </div>
          </div>
        )}

          {displayProducts.length > 0 && (
            <Pagination
              current={currentPage}
              onChange={(page) => setCurrentPage(page)}
              pageSize={pageSize}
              total={displayProducts.length}
              align="center"
              size="large"
              className="search__pagination"
              showSizeChanger={false}
            />
          )}
        </div>
      </div>
    </>
  )
}

export default Search;