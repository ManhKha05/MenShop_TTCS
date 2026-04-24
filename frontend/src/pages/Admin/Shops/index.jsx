import "./Shop.scss";
import { IoSearchSharp } from "react-icons/io5";
import { Popconfirm, Select, Spin, Table, Tag } from "antd";
import { use, useEffect, useState } from "react";
import { MdLock } from "react-icons/md";
import { IoIosUnlock } from "react-icons/io";
import { Link } from "react-router-dom"
import { get } from "../../../utils/request";
import { formatDate } from "../../../utils/date";


function Shops() {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(12);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("");
  const [sort, setSort] = useState("newest");
  const [stats, setStats] = useState({});
  const [shops, setShops] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchApi = async () => {
      try {
        setLoading(true);
        const [statsRes, shopsRes] = await Promise.all([
          get("admin/shops/stats"),
          get("admin/shops", {
            page: page - 1,
            size: pageSize,
            keyword,
            status,
            sort
          })
        ]);

        const statsData = await statsRes.json();
        const shopsData = await shopsRes.json();

        setStats(statsData);
        setShops(shopsData.content)
        setTotal(shopsData.totalElements);
        setLoading(false);
      } catch (error) {
        console.error(error);
      }
    }
    fetchApi();
  }, [keyword, status, sort, page, pageSize])

  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
    }
  }

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      // width: '90px'
    },
    {
      title: 'Logo Shop',
      key: 'logo',
      render: ({ logo }) => (
        <img src={logo} alt="" className="admin-shops__table__image" />
      )
    },
    {
      title: 'Tên cửa hàng',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Chủ cửa hàng',
      dataIndex: 'ownerName',
      key: 'ownerName',
    },
    {
      title: 'Email liên hệ',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Số điện thoại',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: 'Ngày tạo',
      key: 'createdAt',
      render: ({ createdAt }) => (
        formatDate(createdAt)
      )
    },
    {
      title: 'Trạng thái',
      key: 'status',
      render: ({ status }) => {
        switch (status) {
          case "ACTIVE":
            return <Tag color="green">Đang hoạt động</Tag>

          case "PENDING":
            return <Tag color="gold">Chờ duyệt</Tag>

          case "INACTIVE":
            return <Tag color="default">Tạm ngừng</Tag>

          case "BANNED":
            return <Tag color="red">Bị khóa</Tag>

          case "REJECTED":
            return <Tag color="volcano">Bị từ chối</Tag>
        }
      },
    },
    {
      title: 'Thao tác',
      key: 'action',
      render: (record) => (
        <div className="admin-shops__table__actions">
          <Link to={`/admin/shops/${record.id}`} className="view">Xem chi tiết</Link>
          {/* <Popconfirm
            title={record.status === 'BANNED' ? "Mở khóa Shop" : "Khóa Shop"}
            okText={record.status === 'BANNED' ? "Mở khóa" : "Khóa"}
            cancelText="Hủy"
          >
            {record.status === 'BANNED' ? (
              <button className="unblock"><IoIosUnlock /></button>
            ) : (
              record.status === 'ACTIVE' && <button className="block"><MdLock /></button>
            )}
          </Popconfirm> */}
        </div>
      )
    },
  ];

  if (loading) {
    return (
      <div className="dashboard-admin dashboard-admin--loading">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <>
      <div className="admin-shops">
        {/* <div className="admin-shops__title">
          Quản lí Cửa hàng
        </div> */}

        <div className="admin-shops__stats">
          <div className="admin-shops__stat">
            <div className="admin-shops__stat__title">
              Tổng số Cửa hàng
            </div>
            <div className="admin-shops__stat__value">
              {stats?.total}
            </div>
          </div>

          <div className="admin-shops__stat">
            <div className="admin-shops__stat__title">
              Yêu cầu mở Shop mới
            </div>
            <div className="admin-shops__stat__value">
              {stats?.request}
            </div>
          </div>

          <div className="admin-shops__stat">
            <div className="admin-shops__stat__title">
              Cửa hàng đang hoạt động
            </div>
            <div className="admin-shops__stat__value">
              {stats?.active}
            </div>
          </div>

          <div className="admin-shops__stat">
            <div className="admin-shops__stat__title">
              Đang tạm khóa
            </div>
            <div className="admin-shops__stat__value">
              {stats?.banned}
            </div>
          </div>
        </div>

        <div className="admin-shops__filter">
          <div className="admin-shops__input">
            <IoSearchSharp />
            <input type="text" placeholder="Tìm tên cửa hàng" onKeyDown={handleSearch} />
          </div>
          <Select
            defaultValue=""
            style={{ width: 180 }}
            onChange={(e) => setStatus(e)}
            className="admin-shops__status"
            options={[
              { value: '', label: 'Tất cả trạng thái' },
              { value: 'PENDING', label: 'Chờ duyệt' },
              { value: 'ACTIVE', label: 'Đang hoạt động' },
              { value: 'INACTIVE', label: 'Tạm ngừng hoạt động' },
              { value: 'BANNED', label: 'Bị khóa' },
              { value: 'REJECTED', label: 'Bị từ chối' }
            ]}
          />

          <Select
            defaultValue="newest"
            style={{ width: 180 }}
            onChange={(e) => setSort(e)}
            className="admin-shops__status"
            options={[
              { value: 'newest', label: 'Mới nhất' },
              { value: 'oldest', label: 'Cũ nhất' },
              // { value: 'asc', label: 'Tên A - Z' }
            ]}
          />
        </div>

        <Table
          rowKey='id'
          dataSource={shops}
          columns={columns}
          className="admin-shops__table"
          pagination={{
            current: page,
            pageSize,
            total,
            showSizeChanger: true,
            pageSizeOptions: [5, 10, 20],
            showTotal: (total) => `Tổng ${total} cửa hàng`
          }}
          onChange={pagination => {
            setPage(pagination.current)
            setPageSize(pagination.pageSize)
          }}
        />

      </div>
    </>
  )
}

export default Shops;