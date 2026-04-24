import "./FlashSale.scss";
import { LuCalendarDays } from "react-icons/lu";
import { IoFlameSharp } from "react-icons/io5";
import { BsClockFill } from "react-icons/bs";
import { FaCalendarTimes } from "react-icons/fa";
import { IoIosAddCircle } from "react-icons/io";
import { IoSearch } from "react-icons/io5";
import { Button, Input, Select, Spin, message } from "antd";
import FlashSaleEdit from "./FlashSaleEdit";
import { useEffect, useState } from "react";
import FlashSaleTable from "../../../components/FlashSaleTable";
import { get, patch } from "../../../utils/request";

function FlashSale() {
  const [messageApi, contextHolder] = message.useMessage();
  const [openEdit, setOpenEdit] = useState(false);
  const [reload, setReload] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState();
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [selectedItem, setSelectedItem] = useState(null);
  const [stats, setStats] = useState({});
  const [flashSales, setFlashSales] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    const fetchApi = async () => {
      try {
        const [statsRes, flashSalesRes] = await Promise.all([
          get('admin/flash-sales/stats'),
          get('admin/flash-sales', {
            page: page - 1,
            size: pageSize,
            keyword,
            status
          })
        ])

        const statsData = await statsRes.json();
        const flashSalesData = await flashSalesRes.json();

        setStats(statsData);
        setFlashSales(flashSalesData.content);
        setTotal(flashSalesData.totalElements);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    }
    fetchApi();
  }, [keyword, status, page, pageSize, reload])


  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
    }
  }

  const handleEnd = async (id) => {
    try {
      const res = await patch(`admin/flash-sales/${id}/end`);
      if (!res.ok) {
        throw new Error()
      }
      messageApi.success('Đã kết thúc flash sale');
      setReload(!reload);

    } catch (error) {
      messageApi.error('Lỗi khi kết thúc');
    }
  }

  const handleDisable = async (id) => {
    try {
      const res = await patch(`admin/flash-sales/${id}/disable`);
      if (!res.ok) {
        throw new Error()
      }
      messageApi.success('Đã xóa flash sale');
      setReload(!reload);

    } catch (error) {
      messageApi.error('Lỗi khi xóa');
    }
  }

  if (loading) {
    return (
      <div className="dashboard-admin dashboard-admin--loading">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <>
      {contextHolder}
      <div className="flashsale-admin">
        <div className="stats">
          <div className="stat">
            <div className="stat__icon" style={{ color: "#0284c7", backgroundColor: "#e0f2fe" }}>
              <LuCalendarDays />
            </div>
            <div className="stat__info">
              <div className="stat__title">
                Tổng số Flash Sale
              </div>
              <div className="stat__value">
                {stats?.total}
              </div>
            </div>
          </div>

          <div className="stat">
            <div className="stat__icon" style={{ color: "#dc2626", backgroundColor: "#fee2e2" }}>
              <IoFlameSharp />
            </div>
            <div className="stat__info">
              <div className="stat__title">
                Đang chạy
              </div>
              <div className="stat__value">
                {stats?.active}
              </div>
            </div>
          </div>

          <div className="stat">
            <div className="stat__icon" style={{ color: "#ca8a04", backgroundColor: "#fef9c3" }}>
              <BsClockFill />
            </div>
            <div className="stat__info">
              <div className="stat__title">
                Sắp diễn ra
              </div>
              <div className="stat__value">
                {stats?.upcoming}
              </div>
            </div>
          </div>

          <div className="stat">
            <div className="stat__icon" style={{ color: "#374151", backgroundColor: "#e5e7eb" }}>
              <FaCalendarTimes />
            </div>
            <div className="stat__info">
              <div className="stat__title">
                Đã kết thúc
              </div>
              <div className="stat__value">
                {stats?.ended}
              </div>
            </div>
          </div>
        </div>

        <Button
          size="large"
          type="primary"
          icon={<IoIosAddCircle />}
          className="btn-add"
          onClick={() => {
            setOpenEdit(true);
            setSelectedItem(null);
          }}
        >
          Tạo Flash Sale
        </Button>

        <div className="filter">
          <Input placeholder="Tìm tên chương trình" prefix={<IoSearch />} onKeyDown={handleSearch} />
          <Select
            defaultValue=""
            style={{ width: 190 }}
            onChange={(e) => setStatus(e)}
            options={[
              { value: '', label: 'Tất cả trạng thái' },
              { value: 'UPCOMING', label: 'Sắp diễn ra' },
              { value: 'ACTIVE', label: 'Đang diễn ra' },
              { value: 'ENDED', label: 'Đã kết thúc' }
            ]}
          />
        </div>

        <div className="table">
          <FlashSaleTable
            role="ADMIN"
            data={flashSales}
            page={page}
            pageSize={pageSize}
            total={total}
            onEdit={(record) => {
              setOpenEdit(true);
              setSelectedItem(record);
            }}
            onEnd={handleEnd}
            onDisable={handleDisable}
            onPageChange={(p) => setPage(p)}
            onPageSizeChange={(s) => {
              setPageSize(s);
              setPage(1);
            }}
          />
        </div>
      </div>

      <FlashSaleEdit
        open={openEdit}
        record={selectedItem}
        onReload={() => {
          setReload(!reload);
          setOpenEdit(false);
        }}
        onCancel={() => {
          setOpenEdit(false);
          setSelectedItem(null);
        }}
      />
    </>
  )
}

export default FlashSale;