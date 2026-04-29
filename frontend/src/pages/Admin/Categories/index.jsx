import "./Categories.scss";
import { IoAddCircleSharp } from "react-icons/io5";
import { MdCategory } from "react-icons/md";
import { FaBoxArchive } from "react-icons/fa6";
import { MdHideImage } from "react-icons/md";
import { FaArrowTrendUp } from "react-icons/fa6";
import { MdEditSquare } from "react-icons/md";
import { Input, Select, Spin, Table, Tag, Tooltip } from "antd";
import { useEffect, useState } from "react";
import CategoryEdit from "./CategoryEdit";
import { get } from "../../../utils/request"
import { formatDateTime } from "../../../utils/date";


function Categories() {
  const [openModal, setOpenModal] = useState(false);
  const [reload, setReload] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(12);
  const [keyword, setKeyword] = useState("");
  const [status, setStatus] = useState("ALL");
  const [parentId, setParentId] = useState();
  const [stats, setStats] = useState();
  const [categories, setCategories] = useState([]);
  const [parents, setParents] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchApi = async () => {
      try {
        setLoading(true);
        const [statsRes, categoriesRes, parentsRes] = await Promise.all([
          get("admin/categories/stats"),
          get("admin/categories", {
            page: page - 1,
            size: pageSize,
            keyword,
            parentId: parentId === "ALL" ? null : parentId,
            status: status === "ALL" ? null : status
          }),
          get("categories/parents")
        ])

        const statsData = await statsRes.json();
        const categoriesData = await categoriesRes.json();
        const categoriesParentsData = await parentsRes.json();

        setStats(statsData);
        setCategories(categoriesData.content);
        setTotal(categoriesData.totalElements);
        setParents(categoriesParentsData);
        setLoading(false);
      } catch (error) {
        console.log("Loi fetch Categories: ", error);
      }
    }
    fetchApi();
  }, [keyword, status, parentId, page, pageSize, reload])

  // if (loading) {
  //   return (
  //     <div className="dashboard-admin dashboard-admin--loading">
  //       <Spin size="large" />
  //     </div>
  //   );
  // }


  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
    }
  }

  const columns = [
    {
      key: 'id',
      title: "ID",
      dataIndex: "id",
    },
    {
      key: 'imageUrl',
      title: "Ảnh",
      render: ({ imageUrl }) => (
        <img src={imageUrl} alt="" className="admin-categories__image" />
      )
    },
    {
      key: 'name',
      title: "Tên danh mục",
      dataIndex: "name",
    },
    {
      key: 'parentName',
      title: "Danh mục cha",
      render: ({ parentName }) => {
        return parentName ? parentName : "-"
      }
    },
    // {
    //   key: 'totalProduct',
    //   title: "Sản phẩm",
    //   dataIndex: "totalProduct",
    // },
    {
      key: 'id',
      title: "Trạng thái",
      render: ({ status }) =>
        status === "ACTIVE" ? (
          <Tag color="green">Hiển thị</Tag>
        ) : (
          <Tag color="red">Tạm ẩn</Tag>
        ),
    },
    {
      key: 'createdAt',
      title: "Ngày tạo",
      render: ({ createdAt }) => formatDateTime(createdAt)
    },
    {
      key: 'action',
      title: "Thao tác",
      render: (_, record) => (
        <div className="admin-categories__action">
          <Tooltip title="Chỉnh sửa">
            <button
              className="admin-categories__action-btn"
              onClick={() => {
                setOpenModal(true)
                setSelectedCategory(record);
              }}
            >
              <MdEditSquare />
            </button>
          </Tooltip>
          {/* <button></button> */}
        </div>
      ),
    },
  ];

  return (
    <>
      <div className="admin-categories">
        <div className="admin-categories__top">
          <div>
            <div className="admin-categories__title">
              Danh mục
            </div>
            <div className="admin-categories__subtitle">
              Quản lí cấu trúc nghành hàng thời trang nam trên hệ thống
            </div>
          </div>

          <button className="admin-categories__add" onClick={() => { setOpenModal(true); setSelectedCategory(null) }} >
            <IoAddCircleSharp />
            Thêm danh mục mới
          </button>
        </div>

        <div className="admin-categories__stat">
          <div className="admin-categories__stat-item" style={{backgroundColor: "#e0f2fe"}}>
            <div className="admin-categories__stat-item__icon" style={{color: "#0284c7"}}>
              <MdCategory />
            </div>
            <div className="admin-categories__stat-item__title">
              Tổng danh mục
            </div>
            <div className="admin-categories__stat-item__value">
              {stats?.total}
            </div>
          </div>

          <div className="admin-categories__stat-item" style={{backgroundColor: "#dcfce7"}}>
            <div className="admin-categories__stat-item__icon" style={{color: "#16a34a"}}>
              <FaBoxArchive />
            </div>
            <div className="admin-categories__stat-item__title">
              Đang hiển thị
            </div>
            <div className="admin-categories__stat-item__value">
              {stats?.active}
            </div>
          </div>

          <div className="admin-categories__stat-item" style={{backgroundColor: "#fee2e2"}}>
            <div className="admin-categories__stat-item__icon" style={{color: "#dc2626"}}>
              <MdHideImage />
            </div>
            <div className="admin-categories__stat-item__title">
              Đang ẩn
            </div>
            <div className="admin-categories__stat-item__value">
              {stats?.inactive}
            </div>
          </div>

          <div className="admin-categories__stat-item" style={{backgroundColor: "#f3e8ff"}}>
            <div className="admin-categories__stat-item__icon" style={{color: "#9333ea"}}> 
              <FaArrowTrendUp />
            </div>
            <div className="admin-categories__stat-item__title">
              Số danh mục cha
            </div>
            <div className="admin-categories__stat-item__value">
              {stats?.parent}
            </div>
          </div>
        </div>

        <div className="admin-categories__body">
          <div className="admin-categories__filter">
            <div className="admin-categories__search">
              {/* <IoSearch /> */}
              <Input
                placeholder="Tìm kiếm theo tên danh mục..."
                onKeyDown={(e) => handleSearch(e)}
              />
            </div>

            <div className="admin-categories__status">
              <Select
                defaultValue="ALL"
                style={{ width: 200 }}
                onChange={(e) => setParentId(e)}
                options={[
                  { value: 'ALL', label: 'Tất cả danh mục cha' },
                  ...parents.map(c => ({
                    value: c.id, label: c.name
                  }))
                ]}
              />
            </div>

            <div className="admin-categories__status">
              <Select
                defaultValue="ALL"
                style={{ width: 170 }}
                onChange={(e) => setStatus(e)}
                options={[
                  { value: 'ALL', label: 'Tất cả trạng thái' },
                  { value: 'ACTIVE', label: 'Hiển thị' },
                  { value: 'INACTIVE', label: 'Đang ẩn' }
                ]}
              />
            </div>
          </div>

          <Table
            rowKey="id"
            dataSource={categories}
            columns={columns}
            pagination={{
              current: page,
              pageSize,
              total,
              showSizeChanger: true,
              pageSizeOptions: [5, 10, 20],
              showTotal: (total) => `Tổng ${total} danh mục`
            }}
            onChange={pagination => {
              setPage(pagination.current)
              setPageSize(pagination.pageSize)
            }}
            expandable={{
              expandIcon: () => null
            }}
          />
        </div>
      </div >

      <CategoryEdit
        parents={parents}
        category={selectedCategory}
        open={openModal}
        onCancel={() => {
          setOpenModal(false)
          setSelectedCategory(null)
        }}
        onReload={() => {
          setOpenModal(false);
          setReload(!reload)
        }}
      />
    </>
  )
}

export default Categories;