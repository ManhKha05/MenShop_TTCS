import "./Search.scss";
import { IoIosArrowDown } from "react-icons/io";
import { Link } from "react-router-dom"
import { Breadcrumb, Pagination } from "antd"
import { useState } from "react";
import ModalFilter from "./ModalFilter";
import ProductItem from "./ProductItem";

function Search() {
  const [showSort, setShowSort] = useState(false);
  const [sort, setSort] = useState({ value: "default", title: "Phổ biến" });


  console.log(sort);

  const sortOptions = [
    { value: "default", title: "Phổ biến" },
    { value: "top_seller", title: "Bán chạy" },
    { value: "newest", title: "Hàng mới" },
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
                title: 'Kết quả tìm kiếm "laptop"'
              },
            ]}
          />

          <div className="search__top">
            <div className="search__result">
              Tìm thấy 123 sản phẩm cho kết quả tìm kiếm "laptop"
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
                          setShowSort(false); // đóng dropdown
                        }}
                      >
                        {item.title}
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <span style={{ color: 'rgb(128, 128, 137)' }} >Bộ lọc</span>
              <ModalFilter />
            </div>
          </div>

          <div className="product__list">
            <ProductItem />
            <ProductItem />
            <ProductItem />
            <ProductItem />
            <ProductItem />
            <ProductItem />
          </div>

          <Pagination
            defaultCurrent={1}
            total={50}
            align="center"
            size="large"
            className="search__pagination"
          />
        </div>
      </div>
    </>
  )
}

export default Search;