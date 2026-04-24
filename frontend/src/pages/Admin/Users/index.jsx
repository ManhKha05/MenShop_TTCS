import "./Users.scss";
import { FaUsers } from "react-icons/fa";
import { FaUserPlus } from "react-icons/fa6";
import { FaShop } from "react-icons/fa6";
import { MdShoppingCart } from "react-icons/md";
import { Button, Input, Select, Tag, Table, Popconfirm, notification, Spin } from "antd";
import { useEffect, useState } from "react";
import { get, put } from "../../../utils/request";
import { formatDateTime } from "../../../utils/date";

function Users() {
  const [notificationApi, contextHolder] = notification.useNotification();
  const [reload, setReload] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(12);
  const [keyword, setKeyword] = useState("");
  const [role, setRole] = useState();
  const [status, setStatus] = useState();
  const [stats, setStats] = useState({});
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    const fetchApi = async () => {
      try {
        const [statsRes, usersRes] = await Promise.all([
          get("admin/users/stats"),
          get("admin/users", {
            page: page - 1,
            size: pageSize,
            keyword,
            role,
            status
          })
        ])

        const statsData = await statsRes.json();
        const usersData = await usersRes.json();

        setStats(statsData);
        setUsers(usersData.content);
        setTotal(usersData.totalElements);
        
      } catch (error) {
        console.log("Loi Api: ", error)
      } finally {
        setLoading(false);
      }
    }
    fetchApi();
  }, [keyword, role, status, page, pageSize, reload])


  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
    }
  }

  const handleStatus = (record) => {
    const fetchApi = async () => {
      try {
        let res = null;

        if (record.status === "ACTIVE") {
          res = await put(`admin/users/${record.id}/lock`);
        } else {
          res = await put(`admin/users/${record.id}/unlock`);
        }

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message || "Có lỗi xảy ra");
        }

        setReload(!reload);

        notificationApi.success({
          title: 'Cập nhật thành công',
          description: (record.status === "ACTIVE" ? "Khóa " : "Mở khóa ") + `người dùng ${record.fullName} thành công!`
        })

      } catch (error) {
        console.error(error.message);
      }
    }
    fetchApi();
  }


  const columns = [
    {
      key: 'id',
      title: "ID",
      dataIndex: "id",
    },
    {
      key: 'avtar',
      title: "Avatar",
      render: ({ avatar }) => (
        <img src={avatar} alt="" className="admin-users__image" />
      )
    },
    {
      key: 'fullName',
      title: "Tên",
      dataIndex: "fullName",
    },
    {
      key: 'email',
      title: "Email",
      dataIndex: "email",
    },
    {
      key: 'phone',
      title: "Số điện thoại",
      dataIndex: "phone",
    },
    {
      key: 'role',
      title: "Vai trò",
      render: ({ roles }) => {
        if (roles.includes("ADMIN")) {
          return <div className="admin-users__status admin-users__status-shop">Quản trị viên</div>
        } else if (roles.includes("SHOP")) {
          return <div className="admin-users__status admin-users__status-shop">Cửa hàng</div>
        } else {
          return <div className="admin-users__status admin-users__status-customer">Khách hàng</div>
        }
      }
    },
    {
      key: 'createdAt',
      title: "Ngày tham gia",
      render: ({ createdAt }) => {
        return formatDateTime(createdAt)
      }
    },
    {
      key: 'id',
      title: "Trạng thái",
      render: ({ status }) =>
        status === "ACTIVE" ? (
          <Tag color="green">Hoạt động</Tag>
        ) : (
          <Tag color="red">Đã khóa</Tag>
        )
    },
    {
      key: 'action',
      title: "Thao tác",
      render: (_, record) => (
        <div className="admin-users">
          {!record.roles.includes("ADMIN") &&
            (record.status === "ACTIVE" ? (
              <Popconfirm
                title="Khóa người dùng"
                description="Bạn chắc chắn muốn khóa người dùng này?"
                onConfirm={() => handleStatus(record)}
                okText="Khóa"
                cancelText="Hủy"
              >
                <Button type="link" danger>
                  Khóa
                </Button>
              </Popconfirm>
            ) : (
              <Popconfirm
                title="Mở khóa người dùng"
                description="Bạn chắc chắn muốn mở khóa người dùng này?"
                onConfirm={() => handleStatus(record)}
                okText="Mở khóa"
                cancelText="Hủy"
              >
                <Button type="link">
                  Mở khóa
                </Button>
              </Popconfirm>
            ))}
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
      {contextHolder}
      <div className="admin-users">
        {/* <div className="admin-users__title">
          Quản lí người dùng
        </div> */}
        <div className="admin-users__stat__list">
          <div className="admin-users__stat__item">
            <div className="admin-users__stat__icon" style={{ color: "#1677ff", background: "#e6f4ff" }}>
              <FaUsers />
            </div>
            <div className="admin-users__stat__title">
              Tổng người dùng
            </div>
            <div className="admin-users__stat__value">
              {stats?.total}
            </div>
          </div>

          <div className="admin-users__stat__item">
            <div className="admin-users__stat__icon" style={{ color: "#13c2c2", background: "#e6fffb" }}>
              <FaUserPlus />
            </div>
            <div className="admin-users__stat__title">
              Đăng ký mới (tháng)
            </div>
            <div className="admin-users__stat__value">
              {stats?.month}
            </div>
          </div>

          <div className="admin-users__stat__item">
            <div className="admin-users__stat__icon" style={{ color: "#fa8c16", background: "#fff7e6" }}>
              <FaShop />
            </div>
            <div className="admin-users__stat__title">
              Chủ cửa hàng
            </div>
            <div className="admin-users__stat__value">
              {stats?.shop}
            </div>
          </div>

          <div className="admin-users__stat__item">
            <div className="admin-users__stat__icon" style={{ color: "#722ed1", background: "#f9f0ff" }}>
              <MdShoppingCart />
            </div>
            <div className="admin-users__stat__title">
              Khách hàng hoạt động
            </div>
            <div className="admin-users__stat__value">
              {stats?.customerActive}
            </div>
          </div>
        </div>

        <div className="admin-users__content">
          <div className="admin-users__filter">
            <Input
              placeholder="Tìm tên, email, SĐT ..."
              style={{ width: '500px' }}
              onKeyDown={handleSearch}
            />

            <Select
              defaultValue=""
              style={{ width: 170 }}
              onChange={(e) => setRole(e)}
              options={[
                { value: '', label: 'Tất cả vai trò' },
                { value: 'CUSTOMER', label: 'Khách hàng' },
                { value: 'SHOP', label: 'Cửa hàng' }
              ]}
            />

            <Select
              defaultValue=""
              style={{ width: 190 }}
              onChange={(e) => setStatus(e)}
              options={[
                { value: '', label: 'Tất cả trạng thái' },
                { value: 'ACTIVE', label: 'Hoạt động' },
                { value: 'LOCKED', label: 'Đã khóa' }
              ]}
            />
          </div>

          <Table
            rowKey="id"
            dataSource={users}
            columns={columns}
            pagination={{
              current: page,
              pageSize,
              total,
              showSizeChanger: true,
              pageSizeOptions: [5, 10, 20],
              showTotal: (total) => `Tổng ${total} nguời dùng`
            }}
            onChange={pagination => {
              setPage(pagination.current)
              setPageSize(pagination.pageSize)
            }}
          />
        </div>
      </div>
    </>
  )
}

export default Users;