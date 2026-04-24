import "./ShopDetail.scss";
import { FaFingerprint } from "react-icons/fa";
import { MdCalendarToday } from "react-icons/md";
import { MdLock } from "react-icons/md";
import { FaUnlockKeyhole } from "react-icons/fa6";
import { Row, Col, Descriptions, Tag, Statistic, Tabs, notification } from "antd"
import { FaRegStarHalfStroke } from "react-icons/fa6";
import ProductTable from "../../../components/ProductTable";
import OrderTable from "../../../components/OrderTable";
import { useEffect, useState } from "react";
import { get, put } from "../../../utils/request";
import { useNavigate, useParams } from "react-router-dom";
import { formatDate } from "../../../utils/date";
import { FaBan } from "react-icons/fa";
import { IoShieldCheckmarkSharp } from "react-icons/io5";

function ShopDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [reload, setReload] = useState(false);
  const [notificationApi, contextHolder] = notification.useNotification();
  const [shop, setShop] = useState({});

  const [products, setProducts] = useState([]);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);


  useEffect(() => {
    const fetchApi = async () => {
      try {
        const [shopRes, productsRes] = await Promise.all([
          get(`admin/shops/${id}`),
          get(`admin/products`, {
            page: page - 1,
            size: pageSize,
            shopId: id
          })
        ])

        if(shopRes.status === 404) {
          navigate("/404")
        }

        const shopData = await shopRes.json();
        const productsData = await productsRes.json();

        setShop(shopData);
        setProducts(productsData.content || []);
        setTotal(productsData.totalElements || 0);
      } catch (error) {
        console.log(error);
      }
    }
    fetchApi();
  }, [id, reload, page, pageSize])

  console.log(products);

  const handleTableChange = (pagination) => {
    setPage(pagination.current);
    setPageSize(pagination.pageSize);
  };

  const handleStatus = async (v) => {
    try {
      const res = await (put(`admin/shops/${id}/status`, {
        status: v
      }))

      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message);
      }

      notificationApi.success({
        title: "Cập nhật trạng thái thành công!"
      })
      setReload(!reload);
    } catch (error) {
      console.error(error);
    }
  }

  return (
    <>
      {contextHolder}
      <div className="admin-shop">
        <div className="admin-shop__top">
          <img src={shop.logo} alt="" className="admin-shop__top__image" />

          <div>
            <div className="admin-shop__top__name">
              {shop.name}
            </div>
            <div className="admin-shop__top__info">
              <div className="admin-shop__top__desc">
                <FaFingerprint />
                <span>ID: {shop.id}</span>
              </div>
              <div className="admin-shop__top__desc">
                <MdCalendarToday />
                <span>Tham gia: {formatDate(shop.createdAt)}</span>
              </div>
            </div>
          </div>

          {(() => {
            switch (shop.status) {
              case "PENDING":
                return (
                  <>
                    <button
                      className="admin-shop__top__btn admin-shop__top__btn--approve"
                      onClick={() => handleStatus("ACTIVE")}
                    >
                      <IoShieldCheckmarkSharp />
                      Duyệt cửa hàng
                    </button>
                    <button
                      className="admin-shop__top__btn admin-shop__top__btn--lock"
                      onClick={() => handleStatus("REJECTED")}
                    >
                      <FaBan />
                      Từ chối
                    </button>
                  </>
                )

              case "ACTIVE":
                return (
                  <button
                    className="admin-shop__top__btn admin-shop__top__btn--lock"
                    onClick={() => handleStatus("INACTIVE")}
                  >
                    <MdLock />
                    Khóa cửa hàng
                  </button>
                )

              case "INACTIVE":
                return (
                  <button
                    className="admin-shop__top__btn admin-shop__top__btn--unlock"
                    onClick={() => handleStatus("ACTIVE")}
                  >
                    <FaUnlockKeyhole />
                    Mở khóa cửa hàng
                  </button>
                )

              default:
                return ""
            }
          })()}
        </div>


        <div className="admin-shop__body">
          <Row gutter={20}>
            <Col span={16}>
              <div className="admin-shop__info">
                <div className="admin-shop__info__title">
                  Thông tin cửa hàng
                </div>
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="Tên cửa hàng">
                    {shop.name}
                  </Descriptions.Item>

                  <Descriptions.Item label="Chủ cửa hàng">
                    {shop.ownerName}
                  </Descriptions.Item>

                  <Descriptions.Item label="Email">
                    {shop.email}
                  </Descriptions.Item>

                  <Descriptions.Item label="SĐT">
                    {shop.phone}
                  </Descriptions.Item>

                  <Descriptions.Item label="Địa chỉ">
                    {shop.address}
                  </Descriptions.Item>

                  <Descriptions.Item label="Ngày tạo">
                    {formatDate(shop.createdAt)}
                  </Descriptions.Item>

                  <Descriptions.Item label="Trạng thái">
                    {(() => {
                      switch (shop.status) {
                        case "ACTIVE":
                          return <Tag color="green">Đang hoạt động</Tag>
                        case "PENDING":
                          return <Tag color="gold">Chờ duyệt</Tag>
                        case "INACTIVE":
                          return <Tag color="default">Tạm ngừng</Tag>
                        // case "BANNED":
                        //   return <Tag color="red">Bị khóa</Tag>
                        case "REJECTED":
                          return <Tag color="volcano">Bị từ chối</Tag>
                        default:
                          return ""
                      }
                    })()}
                  </Descriptions.Item>
                </Descriptions>
              </div>
            </Col>

            <Col span={8}>
              <div className="admin-shop__stat">
                <div className="admin-shop__stat__title">
                  Thống kê shop
                </div>
                <div className="admin-shop__stat__content">
                  <Row gutter={[16, 20]}>
                    <Col span={12}>
                      <Statistic title="Tổng sản phẩm" value={shop.totalProducts} />
                    </Col>
                    <Col span={12}>
                      <Statistic title="Tổng đơn hàng" value={shop.totalOrders} />
                    </Col>
                    {/* <Col span={12}>
                      <Statistic title="Doanh thu" value={120000000} suffix="đ" />
                    </Col>
                    <Col span={12}>
                      <Statistic title="Đánh giá trung bình" value={4.8} prefix={<FaRegStarHalfStroke />} />
                    </Col> */}
                  </Row>
                </div>
              </div>
            </Col>

            <Col span={24}>
              <div className="admin-shop__tab">
                <Tabs
                  items={[
                    {
                      key: "products",
                      label: "Sản phẩm",
                      children: (
                        <ProductTable
                          role="ADMIN"
                          products={products}
                          page={page}
                          pageSize={pageSize}
                          total={total}
                          onChange={handleTableChange}
                        />
                      )
                    },
                    // { key: "orders", label: "Đơn hàng", children: <OrderTable /> }
                  ]}
                />
              </div>
            </Col>
          </Row>
        </div>
      </div>
    </>
  )
}

export default ShopDetail;