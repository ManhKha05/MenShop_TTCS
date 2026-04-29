import { Button, Input, Pagination, Select, Spin, Tag } from "antd";
import "./Banners.scss";
import { IoSearch } from "react-icons/io5";
import BannerEdit from "./BannerEdit";
import { useEffect, useState } from "react";
import { IoAddCircle } from "react-icons/io5";
import { get } from "../../../utils/request";
import { formatDate } from "../../../utils/date";

function Banners() {
  const [openModal, setOpenModal] = useState(false);
  const [reload, setReload] = useState(false);
  const [selectedBanner, setSelectedBanner] = useState({})
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(6);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [banners, setBanners] = useState([]);
  const [stats, setStats] = useState();
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    const fetchApi = async () => {
      try {
        const [bannersRes, statsRes] = await Promise.all([
          get("admin/banners", {
            page: page - 1,
            size: pageSize,
            status: status === 'ALL' ? null : status,
            keyword
          }),
          get("admin/banners/stats")
        ]);

        const bannersData = await bannersRes.json();
        const statsData = await statsRes.json();

        setBanners(bannersData.content);
        setTotal(bannersData.totalElements);
        setStats(statsData);

      } catch (error) {
        console.log("Lỗi admin banners: ", error);
      } finally {
        setLoading(false);
      }
    }
    fetchApi();
  }, [keyword, status, page, reload])


  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
    }
  }
  
  // if (loading) {
  //   return (
  //     <div className="dashboard-admin dashboard-admin--loading">
  //       <Spin size="large" />
  //     </div>
  //   );
  // }

  return (
    <>
      <div className="banners-admin">
        <div className="banners-admin__top">
          <div className="banners-admin__top__item">
            <div className="banners-admin__top__item__title">
              Tổng số Banner:
            </div>
            <div className="banners-admin__top__item__value">
              {stats?.total}
            </div>
          </div>

          <div className="banners-admin__top__item">
            <div className="banners-admin__top__item__title">
              Tổng số Banner đang hiển thị:
            </div>
            <div className="banners-admin__top__item__value">
              {stats?.active}
            </div>
          </div>

          <Button
            type="primary"
            className="banners-admin__btn"
            onClick={() => {
              setOpenModal(true)
              setSelectedBanner(null)
            }}
          >
            <IoAddCircle />
            Thêm Banner
          </Button>
        </div>

        <div className="banners-admin__filter">
          <Input
            placeholder="Tìm kiếm theo tiêu đề ..."
            prefix={<IoSearch />}
            styles={{ width: '300px' }}
            onKeyDown={(e) => handleSearch(e)}
          />
          {/* <Select
            defaultValue="newest"
            style={{ width: 160 }}
            onChange={(e) => setStatus(e)}
            options={[
              { value: 'newest', label: 'Mới nhất' },
              { value: 'oldest', label: 'Cũ nhất' },
            ]}
          /> */}
          <Select
            defaultValue="ALL"
            style={{ width: 200 }}
            onChange={(e) => setStatus(e)}
            options={[
              { value: 'ALL', label: 'Tất cả trạng thái' },
              { value: 'ACTIVE', label: 'Đang hiển thị' },
              { value: 'INACTIVE', label: 'Đang ẩn' },
            ]}
          />
        </div>

        <div className="banners-admin__content">

          {banners.map(item => (
            <div className="banners-admin__item" key={item.id}>
              <img src={item.imageUrl} alt="" className="banners-admin__item__image" />
              <div className="banners-admin__item__title">
                {item.title}
              </div>
              <div className="banners-admin__item__create">
                Ngày tạo: <span>{formatDate(item.createdAt)}</span>
              </div>
              <div className="banners-admin__item__status">
                Trạng thái:
                {item.status === "ACTIVE" ? (
                  <Tag variant="outlined" color="blue" >Đang hiển thị</Tag>
                ) : (
                  <Tag variant="outlined" color="orange" >Đang ẩn</Tag>
                )}
              </div>
              <Button onClick={() => {
                setOpenModal(true);
                setSelectedBanner(item)
              }}>
                Chỉnh sửa
              </Button>
            </div>
          ))}


          <div className="banners-admin__pagination">
            <Pagination
              current={page}
              total={total}
              pageSize={pageSize}
              align="center"
              onChange={(page) => {
                setPage(page);
              }}
            />
          </div>
        </div>

      </div>

      <BannerEdit
        openModal={openModal}
        record={selectedBanner}
        onCancel={() => {
          setOpenModal(false)
          setSelectedBanner(null)
        }}
        onReload={() => {
          setOpenModal(false);
          setReload(!reload);
        }}
      />
    </>
  )
}

export default Banners;
