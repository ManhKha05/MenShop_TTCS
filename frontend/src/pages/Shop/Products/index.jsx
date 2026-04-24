import "./Products.scss";
import { FaBoxArchive } from "react-icons/fa6";
import { GrDocumentTime } from "react-icons/gr";
import { AiOutlineStock } from "react-icons/ai";
import { BiSolidHide } from "react-icons/bi";
import { Button, Input, Select, Spin, notification } from "antd";
import { IoMdAdd } from "react-icons/io";
import ProductTable from "../../../components/ProductTable";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { get, put } from "../../../utils/request";
import { connectSocket, disconnectSocket, subscribeSocket } from "../../../utils/socket";

function Products() {
  const [reload, setReload] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [categoryId, setCategoryId] = useState(null);

  const [stats, setStats] = useState({});
  const [categogies, setCategogies] = useState([]);
  const [products, setProducts] = useState([]);
  const shopId = localStorage.getItem("shopId");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    const fetchApi = async () => {
      const [statsRes, categoriesRes, productsRes] = await Promise.all([
        get('shop/products/stats'),
        get('shop/categories'),
        get('shop/products', {
          page: page - 1,
          size: pageSize,
          keyword,
          status,
          categoryId
        })
      ])

      const statsData = await statsRes.json();
      const categoriesData = await categoriesRes.json();
      const productsData = await productsRes.json();

      setStats(statsData);
      setCategogies(categoriesData);
      setProducts(productsData.content);
      setTotal(productsData.totalElements);
      setLoading(false);
    }
    fetchApi();
  }, [page, pageSize, keyword, status, categoryId, reload])

  useEffect(() => {
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket(`/topic/shop-products/${shopId}`, (data) => {
        if (!data) return;

        if (
          data.type === "PRODUCT_UPDATED"
        ) {
          setReload(!reload);
        }
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, [shopId, products]);

  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
      setPage(1);
    }
  }

  const handleTableChange = (pagination) => {
    setPage(pagination.current);
    setPageSize(pagination.pageSize);
  };

  const updateStatus = async (id, action) => {
    try {
      await put(`shop/products/${id}/${action}`);

      let msg = "";
      switch (action) {
        case "inactive":
          msg = "Đã ẩn sản phẩm";
          break;
        case "active":
          msg = "Mở bán sản phẩm thành công";
          break;
        default:
          msg = "Cập nhật thành công";
      }

      notification.success({
        message: "Thành công",
        description: msg,
      });

      // reload lại list
      setReload(!reload);

    } catch (err) {
      notification.error({
        message: "Lỗi",
        description: err.message,
      });
    }
  };

  if (loading) {
    return (
      <div className="dashboard-admin dashboard-admin--loading">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <>
      <div className="shop-products">

        <div className="shop-products__stats">
          <div className="shop-products__stat">
            <div className="shop-products__stat__row">
              <div className="shop-products__stat__title">
                Tổng số sản phẩm
              </div>
              <div className="shop-products__stat__icon" style={{ color: 'blue' }}>
                <FaBoxArchive />
              </div>
            </div>

            <div className="shop-products__stat__value" >
              {stats.totalProducts}
            </div>
          </div>

          <div className="shop-products__stat">
            <div className="shop-products__stat__row">
              <div className="shop-products__stat__title">
                Chờ duyệt
              </div>
              <div className="shop-products__stat__icon" style={{ color: '#d6c01c' }} >
                <GrDocumentTime />
              </div>
            </div>

            <div className="shop-products__stat__value">
              {stats.pending}
            </div>
          </div>

          <div className="shop-products__stat">
            <div className="shop-products__stat__row">
              <div className="shop-products__stat__title">
                Đang bán
              </div>
              <div className="shop-products__stat__icon">
                <AiOutlineStock />
              </div>
            </div>

            <div className="shop-products__stat__value">
              {stats.active}
            </div>
          </div>

          <div className="shop-products__stat">
            <div className="shop-products__stat__row">
              <div className="shop-products__stat__title">
                Hết hàng
              </div>
              <div className="shop-products__stat__icon" style={{ color: 'red' }}>
                <BiSolidHide />
              </div>
            </div>

            <div className="shop-products__stat__value">
              {stats.outOfStock}
            </div>
          </div>
        </div>

        <Link to="/shop/edit-product">
          <Button type="primary" icon={<IoMdAdd />}>Tạo sản phẩm</Button>
        </Link>

        <div className="shop-products__content">
          <div className="shop-products__filter">
            <Input
              placeholder="Tìm sản phẩm ..."
              style={{ width: '500px' }}
              onKeyDown={handleSearch}
            />

            <Select
              defaultValue=""
              style={{ width: 170 }}
              onChange={(value) => {
                setStatus(value);
                setPage(1);
              }}
              options={[
                { value: '', label: 'Tất cả trạng thái' },
                { value: 'PENDING', label: 'Chờ duyệt' },
                { value: 'ACTIVE', label: 'Đang bán' },
                { value: 'INACTIVE', label: 'Tạm ẩn' },
                // { value: 'OUT_OF_STOCK', label: 'Hết hàng' },
                { value: 'REJECTED', label: 'Bị từ chối' }
              ]}
            />

            <Select
              defaultValue=""
              style={{ width: 190 }}
              onChange={(value) => {
                setCategoryId(value);
                setPage(1);
              }}
              options={[
                { value: "", label: "Tất cả danh mục" },
                ...categogies.map(it => (
                  {
                    value: it.id, label: it.name
                  }
                ))
              ]
              }
            />
          </div>

          <ProductTable
            role="SHOP"
            products={products}
            page={page}
            pageSize={pageSize}
            total={total}
            onChange={handleTableChange}
            onInactive={(id) => updateStatus(id, "inactive")}
            onActive={(id) => updateStatus(id, "active")}
          />

        </div>
      </div>
    </>
  )
}

export default Products;