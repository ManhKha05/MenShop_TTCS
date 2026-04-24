import React, { useEffect, useState } from "react";
import {
  Breadcrumb,
  Select,
  Button,
  Slider,
  Checkbox,
  Rate,
  Spin,
  Empty,
  Pagination,
  Radio,
} from "antd";
import { FilterOutlined, AppstoreOutlined } from "@ant-design/icons";
import { Link, useNavigate, useParams } from "react-router-dom";
import { get } from "../../../utils/request";
import "./Category.scss";

function Category() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [showFilter, setShowFilter] = useState(false);
  const [category, setCategory] = useState(null);
  const [products, setProducts] = useState([]);

  const [loadingCategory, setLoadingCategory] = useState(false);
  const [loadingProducts, setLoadingProducts] = useState(false);

  const [sort, setSort] = useState("popular");
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [total, setTotal] = useState(0);

  const [selectedChildIds, setSelectedChildIds] = useState([]);
  const [priceRange, setPriceRange] = useState([0, 2000000]);
  const [rating, setRating] = useState(null);

  useEffect(() => {
    fetchCategory();
  }, [id]);

  useEffect(() => {
    fetchProducts();
  }, [id, page, pageSize, sort]);

  const fetchCategory = async () => {
    try {
      setLoadingCategory(true);

      const res = await get(`categories/${id}`);
      const data = await res.json();

      setCategory(data);

      setSelectedChildIds([]);
      setPage(0);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingCategory(false);
    }
  };

  const fetchProducts = async (customFilter = null) => {
    try {
      setLoadingProducts(true);

      const filters = customFilter || {
        selectedChildIds,
        priceRange,
        rating,
      };

      const params = new URLSearchParams();

      params.append("page", page);
      params.append("size", pageSize);
      params.append("sort", sort);

      if (filters.priceRange?.[0] > 0) {
        params.append("minPrice", filters.priceRange[0]);
      }

      if (filters.priceRange?.[1]) {
        params.append("maxPrice", filters.priceRange[1]);
      }

      if (filters.rating) {
        params.append("rating", filters.rating);
      }

      if (filters.selectedChildIds?.length > 0) {
        filters.selectedChildIds.forEach((childId) => {
          params.append("childCategoryIds", childId);
        });
      }

      const res = await get(`categories/${id}/products?${params.toString()}`);
      const data = await res.json();

      setProducts(data.content || []);
      setTotal(data.totalElements || 0);
    } catch (error) {
      console.error(error);
    } finally {
      setLoadingProducts(false);
    }
  };

  const handleApplyFilter = () => {
    setPage(0);

    fetchProducts({
      selectedChildIds,
      priceRange,
      rating,
    });
  };

  const handleSortChange = (value) => {
    setSort(value);
    setPage(0);
  };

  const handleClickCategory = (categoryId) => {
    navigate(`/danh-muc/${categoryId}`);
  };

  const formatPrice = (price) => {
    if (!price) return "0";
    return Number(price).toLocaleString("vi-VN");
  };

  const getDisplayPrice = (product) => {
    return product.flashSalePrice || product.salePrice || product.price;
  };

  if (loadingCategory) {
    return (
      <div className="category-page">
        <div className="category-container" style={{ textAlign: "center", padding: 80 }}>
          <Spin size="large" />
        </div>
      </div>
    );
  }

  if (!category) {
    return (
      <div className="category-page">
        <div className="category-container">
          <Empty description="Không tìm thấy danh mục" />
        </div>
      </div>
    );
  }

  console.log("categories", category);
  console.log("products", products);

  return (
    <div className="category-page">
      <div className="category-container">
        <Breadcrumb
          className="category-breadcrumb"
          items={[
            {
              title: "Trang chủ",
              onClick: () => navigate("/"),
            },
            {
              title: "Danh mục",
            },
            ...(category.parentName
              ? [
                {
                  title: category.parentName,
                  onClick: () => navigate(`/danh-muc/${category.parentId}`),
                },
              ]
              : []),
            {
              title: category.name,
            },
          ]}
        />

        <section className="category-hero">
          <div>
            <h1>{category.name}</h1>
            <p>
              {category.parentCategory
                ? `Khám phá các sản phẩm nổi bật thuộc danh mục ${category.name}`
                : `Tất cả sản phẩm thuộc danh mục ${category.name}`}
            </p>
          </div>

          <div className="category-hero__stats">
            <strong>{total}</strong>
            <span>sản phẩm</span>
          </div>
        </section>

        <section className="category-section">
          <div className="category-section__header">
            <div>
              <h2>{category.sectionTitle}</h2>
              <p>{category.sectionDescription}</p>
            </div>
          </div>

          <div className="subcategory-grid">
            {(category.categories || []).map((item) => (
              <div
                className="subcategory-card"
                key={item.id}
                onClick={() => handleClickCategory(item.id)}
              >
                <div className="subcategory-card__image">
                  <img
                    src={item.imageUrl || "/images/no-image.png"}
                    alt={item.name}
                  />
                </div>

                <div className="subcategory-card__name">{item.name}</div>
                <div className="subcategory-card__total">
                  {item.totalProduct || 0} sản phẩm
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="category-toolbar">
          <div className="category-toolbar__result">
            Tìm thấy <strong>{total}</strong> sản phẩm trong danh mục{" "}
            <strong>“{category.name}”</strong>
          </div>

          <div className="category-toolbar__actions">
            <span>Sắp xếp</span>

            <Select
              value={sort}
              style={{ width: '150px' }}
              className="category-sort"
              onChange={handleSortChange}
              options={[
                { value: "popular", label: "Phổ biến" },
                { value: "newest", label: "Mới nhất" },
                { value: "price_asc", label: "Giá thấp đến cao" },
                { value: "price_desc", label: "Giá cao đến thấp" },
                { value: "best_selling", label: "Bán chạy" },
                { value: "rating", label: "Đánh giá cao" },
              ]}
            />

            {/* <Button
              icon={<FilterOutlined />}
              onClick={() => setShowFilter(!showFilter)}
            >
              Bộ lọc
            </Button> */}
          </div>
        </section>

        <div className="category-content">
          <aside className={`category-filter ${showFilter ? "show" : ""}`}>
            <div className="category-filter__title">
              <FilterOutlined />
              Bộ lọc sản phẩm
            </div>

            <div className="category-filter__group">
              <h3>{category.parentCategory ? "Danh mục con" : "Danh mục liên quan"}</h3>

              <Checkbox.Group
                className="category-filter__checkbox"
                value={selectedChildIds}
                onChange={setSelectedChildIds}
              >
                {(category.categories || []).map((item) => (
                  <Checkbox key={item.id} value={item.id}>
                    {item.name}
                  </Checkbox>
                ))}
              </Checkbox.Group>
            </div>

            <div className="category-filter__group">
              <h3>Khoảng giá</h3>

              <Slider
                range
                value={priceRange}
                min={0}
                max={2000000}
                step={50000}
                onChange={setPriceRange}
              />

              <div className="category-filter__price">
                <span>{formatPrice(priceRange[0])}đ</span>
                <span>{formatPrice(priceRange[1])}đ</span>
              </div>
            </div>

            <div className="category-filter__group">
              <h3>Đánh giá</h3>

              <Radio.Group
                value={rating}
                onChange={(e) => {
                  if (rating === e.target.value) {
                    setRating(null); // bỏ chọn
                  } else {
                    setRating(e.target.value);
                  }
                }}
                className="category-filter__radio"
              >
                {[5, 4, 3].map((star) => (
                  <Radio key={star} value={star} className="category-filter__radio-item">
                    <div className="category-filter__rating">
                      <Rate disabled defaultValue={star} />
                      <span>từ {star} sao</span>
                    </div>
                  </Radio>
                ))}
                <Radio
                  value={null}
                  onClick={() => setRating(null)}
                >
                  Tất cả
                </Radio>
              </Radio.Group>
            </div>

            <Button
              className="category-filter__apply"
              type="primary"
              block
              onClick={handleApplyFilter}
            >
              Áp dụng
            </Button>
          </aside>

          <main className="category-products">
            <div className="category-products__title">
              <AppstoreOutlined />
              <span>Sản phẩm nổi bật</span>
            </div>

            {loadingProducts ? (
              <div style={{ textAlign: "center", padding: 60 }}>
                <Spin size="large" />
              </div>
            ) : products.length === 0 ? (
              <Empty description="Không có sản phẩm nào" />
            ) : (
              <>
                <div className="product-grid">
                  {products.map((item) => (
                    <Link
                      to={`/san-pham/${item.id}`}
                      className="product-card"
                      key={item.id}
                      onClick={() => navigate(`/san-pham/${item.id}`)}
                    >
                      <div className="product-card__image">
                        <img
                          src={item.image || "/images/no-image.png"}
                          alt={item.name}
                        />
                      </div>

                      <div className="product-card__body">
                        <div className="product-card__price">
                          {formatPrice(getDisplayPrice(item))} <sup>đ</sup>
                        </div>

                        <div className="product-card__shop">
                          {item.shopName}
                        </div>

                        <div className="product-card__name">
                          {item.name}
                        </div>

                        <div className="product-card__meta">
                          <Rate
                            disabled
                            allowHalf
                            value={Number(item.rating || 0)}
                          />
                          <span className="product-card__line" />
                          <span>Đã bán {item.sold || 0}</span>
                        </div>
                      </div>
                    </Link>
                  ))}
                </div>

                <div style={{ display: "flex", justifyContent: "center", marginTop: 24, textAlign: "center" }}>
                  <Pagination
                    current={page + 1}
                    pageSize={pageSize}
                    total={total}
                    // showSizeChanger
                    // pageSizeOptions={[10, 20, 40]}
                    onChange={(current, size) => {
                      setPage(current - 1);
                      setPageSize(size);
                    }}
                  />
                </div>
              </>
            )}
          </main>
        </div>
      </div>
    </div>
  );
}

export default Category;