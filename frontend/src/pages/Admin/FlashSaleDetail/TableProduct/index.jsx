import { Button, Popconfirm, Space, Table, Tag, Tooltip } from "antd";
import "./TableProduct.scss";
import {
  CheckOutlined,
  CloseOutlined,
  EyeOutlined
} from "@ant-design/icons";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { get } from "../../../../utils/request"


function TableProduct({ flashSaleId }) {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(12);
  const [dataSource, setDataSource] = useState([]);

  useEffect(() => {
    fetchData();
  }, [page, pageSize, flashSaleId]);

  const fetchData = async () => {
    const res = await get(`admin/flash-sales/${flashSaleId}/products`, {
      page: page - 1,
      size: pageSize
    });

    const data = await res.json();

    setDataSource(data.content);
    setTotal(data.totalElements);
  };

  console.log(dataSource);

  const columns = [
    {
      title: 'Sản phẩm',
      key: 'info',
      render: ({ name, image }) => (
        <div className="info">
          <img src={image} alt="" className="thumbnail" />
          <div className="name">
            {name}
          </div>
        </div>
      )
    },
    {
      title: 'Shop',
      dataIndex: 'shop',
      key: 'shop',
    },
    {
      title: 'Giá gốc',
      key: 'originalPrice',
      render: ({ originalPrice }) => (
        <div className="original-price">
          {originalPrice}đ
        </div>
      )
    },
    {
      title: 'Giá sale',
      key: 'price',
      render: ({ salePrice, originalPrice }) => {
        const percent = Math.round(
          (1 - salePrice / originalPrice) * 100
        );

        return (
          <>
            <div className="sale-price">{salePrice}đ</div>
            <div className="sale-rate">-{percent}%</div>
          </>
        );
      }
    },
    // {
    //   title: 'Trạng thái',
    //   key: 'status',
    //   render: ({ status }) => {
    //     switch (status) {
    //       case "PENDING":
    //         return <Tag color="gold" variant="outlined">Chờ duyệt</Tag>;

    //       case "APPROVED":
    //         return <Tag color="green" variant="outlined">Đã duyệt</Tag>;

    //       case "REJECTED":
    //         return <Tag color="red" variant="outlined">Bị từ chối</Tag>;

    //       default:
    //         return <Tag>{status}</Tag>;
    //     }
    //   }
    // },
    {
      title: "Thao tác",
      key: "action",
      render: (record) => {

        return (
          <Space>
            <Tooltip title="Xem thông tin sản phẩm">
              <Link to={`/admin/products/${record.id}`}>
                <Button
                  icon={<EyeOutlined />}
                />
              </Link>
            </Tooltip>
          </Space>
        );
      }
    }
  ];

  return (
    <>
      <Table
        rowKey="id"
        dataSource={dataSource}
        columns={columns}
        className="tableproduct-admin"
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: [5, 10, 20],
          showTotal: (total) => `Tổng ${total} sản phẩm`
        }}
        onChange={pagination => {
          setPage(pagination.current)
          setPageSize(pagination.pageSize)
        }}
      />
    </>
  )
}

export default TableProduct;