import { Button, Popconfirm, Space, Table, Tag } from "antd";
import "./ProductTable.scss";
import { useState } from "react";
import { EyeOutlined, EditOutlined, DeleteOutlined, CheckOutlined, CloseOutlined } from "@ant-design/icons";
import { StopOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";


function ProductTable({ role, products, page, pageSize, total, onChange, onApprove, onReject, onActive, onInactive }) {
  const [loadingId, setLoadingId] = useState(null);

  const handleAction = async (id, action) => {
    setLoadingId(id);
    await action(id);
    setLoadingId(null);
  };

  const columns = [
    {
      key: 'id',
      title: "ID",
      dataIndex: "id"
    },
    {
      key: 'image',
      title: "Ảnh",
      render: ({ image }) => (
        <img src={image} alt="" className="product-table__image" />
      )
    },
    {
      key: 'name',
      title: "Tên",
      dataIndex: "name",
      width: 250
    },
    {
      key: 'category',
      title: "Danh mục",
      dataIndex: "category"
    },
    {
      key: 'shop',
      title: "Shop",
      dataIndex: "shop",
      hidden: role === "ADMIN" ? false : true
    },
    {
      key: 'price',
      title: "Giá",
      dataIndex: "price"
    },
    {
      key: 'stock',
      title: "Tồn kho",
      dataIndex: "stock"
    },
    {
      key: 'status',
      title: "Trạng thái",
      render: ({ status }) => {
        switch (status) {
          case "ACTIVE":
            return <Tag variant="outlined" color="green">Đang bán</Tag>

          case "PENDING":
            return <Tag variant="outlined" color="gold">Chờ duyệt</Tag>

          case "INACTIVE":
            return <Tag variant="outlined" color="default">Tạm ẩn</Tag>

          case "OUT_OF_STOCK":
            return <Tag variant="outlined" color="orange">Hết hàng</Tag>

          // case "BANNED":
          //   return <Tag variant="outlined" color="red">Bị khóa</Tag>

          case "REJECTED":
            return <Tag variant="outlined" color="volcano">Bị từ chối</Tag>
        }
      }
    },
    {
      key: 'action',
      title: "Thao tác",
      render: (record) => {
        if (role === "ADMIN") {
          return (
            <Space direction="vertical" size={5}>
              <Link to={`/admin/products/${record.id}`} className="product-table__btn" size="small" icon={<EyeOutlined />}>
                <EyeOutlined /> Xem
              </Link>

              {record.status === "PENDING" && (
                <Space size={4}>
                  <Button size="small" type="primary" icon={<CheckOutlined />}
                    onClick={() => handleAction(record.id, onApprove)}
                    loading={loadingId === record.id}
                  >
                    Duyệt
                  </Button>

                  <Button size="small" danger icon={<CloseOutlined />} onClick={() => handleAction(record.id, onReject)}>
                    Từ chối
                  </Button>
                </Space>
              )}

              {/* {record.status === "ACTIVE" && (
                <Button size="small" danger icon={<StopOutlined />} onClick={() => onInactive(record.id)}>
                  Khóa
                </Button>
              )} */}
            </Space>
          )
        }

        return (
          <Space >
            <Link to={`/shop/products/${record.id}`} className="product-table__btn" size="small" icon={<EyeOutlined />}>

              Xem
            </Link>

            {record.status !== "REJECTED" && (
              <Link to={`/shop/edit-product/${record.id}`}>
                <Button size="small" icon={<EditOutlined />}>
                  Sửa
                </Button>
              </Link>
            )}

            {record.status === "ACTIVE" && (
              <Popconfirm
                title="Ẩn sản phẩm"
                description="Sản phẩm sẽ bị ẩn khỏi người dùng"
                onConfirm={() => handleAction(record.id, onInactive)}
                okText="Ẩn"
                cancelText="Hủy"
              >
                <Button size="small" danger icon={<StopOutlined />}>
                  Ẩn
                </Button>
              </Popconfirm>
            )}
            {record.status === "INACTIVE" && (
              <Button
                size="small"
                type="primary"
                loading={loadingId === record.id}
                onClick={() => handleAction(record.id, onActive)}
              >
                Mở bán
              </Button>
            )}

          </Space>
        )
      }
    }
  ];
  return (
    <>
      <Table
        rowKey="id"
        columns={columns.filter(col => !col.hidden)}
        dataSource={products}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: [5, 10, 20],
          showTotal: (total) => `Tổng ${total} sản phẩm`
        }}
        onChange={onChange}
      />
    </>
  )
}

export default ProductTable;