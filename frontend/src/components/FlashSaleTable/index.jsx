import { Button, Popconfirm, Space, Table, Tag } from "antd";
import { ClockCircleOutlined, FireOutlined, StopOutlined, EyeOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { CgCloseR } from "react-icons/cg";
import dayjs from "dayjs";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";
import { formatDateTime } from "../../utils/date";

function FlashSaleTable({ role, data, page, pageSize, total, onEdit, onRegister, onEnd, onDisable, onPageChange, onPageSizeChange }) {
  const [now, setNow] = useState(dayjs());

  useEffect(() => {
    const interval = setInterval(() => {
      setNow(dayjs());
    }, 1000);

    return () => clearInterval(interval);
  }, [])



  const getStatus = (item) => {
    if (item.isDisabled) return "DISABLED";

    if (now.isBefore(dayjs(item.startTime))) return "UPCOMING";
    if (now.isAfter(dayjs(item.endTime))) return "ENDED";
    return "ACTIVE";
  };


  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: 'Tên chương trình',
      key: 'name',
      render: ({ name }) => {
        return <span style={{ fontSize: '16px', fontWeight: '600' }}>{name}</span>
      }
    },
    {
      title: 'Thời gian bắt đầu',
      key: 'startTime',
      render: ({ startTime }) => formatDateTime(startTime)
    },
    {
      title: 'Thời gian kết thúc',
      key: 'endTime',
      render: ({ endTime }) => formatDateTime(endTime)
    },
    {
      title: 'Trạng thái',
      key: 'status',
      render: (record) => {
        const status = getStatus(record);
        switch (status) {
          case "UPCOMING":
            return (
              <Tag icon={<ClockCircleOutlined />} color="gold">
                Sắp diễn ra
              </Tag>
            );

          case "ACTIVE":
            return (
              <Tag icon={<FireOutlined />} color="red">
                Đang diễn ra
              </Tag>
            );

          case "ENDED":
            return (
              <Tag icon={<StopOutlined />} color="default">
                Đã kết thúc
              </Tag>
            );

          default:
            return <Tag>{status}</Tag>;
        }
      }
    },
    {
      title: "Thao tác",
      key: "action",
      render: (record) => (
        role === "ADMIN" ? (
          <Space>
            <Link to={`/admin/flash-sale/${record.id}`} >
              <Button icon={<EyeOutlined />} >Xem</Button>
            </Link>

            {getStatus(record) === "UPCOMING" && (
              <>
                <Button
                  icon={<EditOutlined />}
                  onClick={() => onEdit(record)}
                >
                  Sửa
                </Button>
                <Popconfirm
                  title="Xóa chương trình?"
                  onConfirm={() => onDisable(record.id)}
                >
                  <Button danger icon={<DeleteOutlined />} />
                </Popconfirm>
              </>
            )}

            {getStatus(record) === "ACTIVE" && (
              <Popconfirm title="Kết thúc flash sale sớm?" onConfirm={() => onEnd(record.id)}>
                <Button danger icon={<CgCloseR />}>Kết thúc</Button>
              </Popconfirm>
            )}
          </Space>
        ) : (
          <Button
            type="primary"
            disabled={getStatus(record) !== "UPCOMING"}
            onClick={() => onRegister(record.id)}
          >
            Đăng ký sản phẩm
          </Button>
        )
      )
    }
  ];

  return (
    <>
      <Table
        rowKey="id"
        dataSource={data}
        columns={columns}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: [5, 10, 20],
          showTotal: (total) => `Tổng ${total} chương trình`
        }}
        onChange={pagination => {
          onPageChange(pagination.current)
          onPageSizeChange(pagination.pageSize)
        }}
      />
    </>
  )
}

export default FlashSaleTable;