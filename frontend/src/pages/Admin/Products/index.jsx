import "./Products.scss";
import { FaBoxArchive } from "react-icons/fa6";
import { GrDocumentTime } from "react-icons/gr";
import { AiOutlineStock } from "react-icons/ai";
import { BiSolidHide } from "react-icons/bi";
import { Input, Select, Spin, notification } from "antd";
import ProductTable from "../../../components/ProductTable";
import { useEffect, useState } from "react";
import { get, put } from "../../../utils/request"
import { connectSocket, subscribeSocket, unsubscribe } from "../../../utils/socket";

function Products() {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [categoryId, setCategoryId] = useState(null);
  const [shopId, setShopId] = useState(null);
  const [stats, setStats] = useState([]);
  const [categories, setCategories] = useState([]);
  const [shops, setShops] = useState([]);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);


  const fetchStats = async () => {
    try {
      const res = await get("admin/products/stats");
      const data = await res.json();
      setStats(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchCategories = async () => {
    try {
      const res = await get("admin/categories/leaf");
      const data = await res.json();
      setCategories(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchShops = async () => {
    try {
      const res = await get("admin/shops/all");
      const data = await res.json();
      setShops(data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchProducts = async () => {
    try {
      const res = await get(`admin/products`, {
        page: page - 1,
        size: pageSize,
        keyword,
        status,
        categoryId,
        shopId
      });
      const data = await res.json();

      setProducts(data.content);
      setTotal(data.totalElements);

    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket(`/topic/product/admin`, (data) => {
        if (!data) return;

        if (
          data.type === "PRODUCT_REQUEST_CREATE"
        ) {
          fetchStats();
          fetchProducts();
        }
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, []);

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

  useEffect(() => {
    setLoading(true);
    fetchProducts();
    fetchStats();
    setLoading(false);
  }, [page, pageSize, keyword, status, categoryId, shopId]);

  useEffect(() => {
    setLoading(true);
    fetchShops();
    fetchCategories();
    setLoading(false);
  }, []);

  if (loading) {
    return (
      <div className="dashboard-admin dashboard-admin--loading">
        <Spin size="large" />
      </div>
    );
  }

  const updateStatus = async (id, action) => {
    try {
      await put(`admin/products/${id}/${action}`);

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
        placement: "topRight",
        showProgress: true
      });

      await Promise.all([fetchProducts(), fetchStats()])
    } catch (err) {
      console.error(err);
    }
  };

  console.log(stats)
  console.log(categories);
  console.log(shops);
  console.log(products);

  return (
    <>
      <div className="admin-products">
        <div className="admin-products__stats">
          <div className="admin-products__stat">
            <div className="admin-products__stat__row">
              <div className="admin-products__stat__title">
                Tổng số sản phẩm
              </div>
              <div className="admin-products__stat__icon" style={{ color: 'blue' }}>
                <FaBoxArchive />
              </div>
            </div>

            <div className="admin-products__stat__value" >
              {stats.totalProducts}
            </div>
          </div>

          <div className="admin-products__stat">
            <div className="admin-products__stat__row">
              <div className="admin-products__stat__title">
                Chờ duyệt
              </div>
              <div className="admin-products__stat__icon" style={{ color: '#d6c01c' }} >
                <GrDocumentTime />
              </div>
            </div>

            <div className="admin-products__stat__value">
              {stats.pending}
            </div>
          </div>

          <div className="admin-products__stat">
            <div className="admin-products__stat__row">
              <div className="admin-products__stat__title">
                Đang bán
              </div>
              <div className="admin-products__stat__icon">
                <AiOutlineStock />
              </div>
            </div>

            <div className="admin-products__stat__value">
              {stats.active}
            </div>
          </div>

          <div className="admin-products__stat">
            <div className="admin-products__stat__row">
              <div className="admin-products__stat__title">
                Tạm ẩn
              </div>
              <div className="admin-products__stat__icon" style={{ color: 'red' }}>
                <BiSolidHide />
              </div>
            </div>

            <div className="admin-products__stat__value">
              {stats.inactive}
            </div>
          </div>
        </div>

        <div className="admin-products__content">
          <div className="admin-products__filter">
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
              showSearch={{ optionFilterProp: 'label' }}
              onChange={(value) => {
                setCategoryId(value === "ALL" ? null : value);
                setPage(1);
              }}
              options={[
                { value: '', label: 'Tất cả danh mục' },
                ...categories.map(it => (
                  { value: it.id, label: it.name }
                ))
              ]}
            />

            <Select
              defaultValue=""
              style={{ width: 190 }}
              showSearch={{ optionFilterProp: 'label' }}
              onChange={(value) => {
                setShopId(value === "" ? null : value);
                setPage(1);
              }}
              options={[
                { value: '', label: 'Tất cả Shop' },
                ...shops.map(it => (
                  { value: it.id, label: it.name }
                ))
              ]}
            />
          </div>

          <ProductTable
            role="ADMIN"
            products={products}
            page={page}
            pageSize={pageSize}
            total={total}
            onChange={handleTableChange}
            onApprove={(id) => updateStatus(id, "approve")}
            onReject={(id) => updateStatus(id, "reject")}
            onInactive={(id) => updateStatus(id, "inactive")}
          />
        </div>
      </div>
    </>
  )
}

export default Products;