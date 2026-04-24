import { useState } from "react";
import "./OrderTable.scss";
import {
  EyeOutlined,
  CheckOutlined,
  CarOutlined,
  CheckCircleOutlined,
  CloseOutlined,
} from "@ant-design/icons";
import { Button, Modal, notification, Space, Table, Tag } from "antd";
import { Link } from "react-router-dom";
import { formatDateTime } from "../../utils/date"
import { patch } from "../../utils/request";

function OrderTable({ orders = [], page = 1, pageSize = 10, total = 0, onChangePage, onReload }) {

  const columns = [
    {
      title: "Mã đơn",
      dataIndex: "orderCode",
      key: "orderCode",
    },
    {
      title: "Khách hàng",
      dataIndex: "receiverName",
      key: "receiverName",
    },
    {
      title: "SĐT",
      dataIndex: "receiverPhone",
      key: "receiverPhone",
    },
    {
      title: "Tổng tiền",
      dataIndex: "finalPrice",
      key: "finalPrice",
    },
    // {
    //   title: "Sản phẩm",
    //   dataIndex: "quantity",
    //   key: "quantity",
    // },
    {
      title: "Thanh toán",
      dataIndex: "paymentMethod",
      key: "paymentMethod",
    },
    {
      title: "Trạng thái",
      key: "status",
      render: ({ status }) => {
        switch (status) {
          case "PENDING":
            return <Tag color="gold">Chờ xác nhận</Tag>;

          case "CONFIRMED":
            return <Tag color="blue">Đã xác nhận</Tag>;

          case "DELIVERING":
            return <Tag color="processing">Đang giao</Tag>;

          case "DELIVERED":
            return <Tag color="green">Đã giao</Tag>;

          case "COMPLETED":
            return <Tag color="green">Hoàn thành</Tag>;

          case "CANCELLED":
            return <Tag color="red">Đã hủy</Tag>;

          default:
            return <Tag>Không xác định</Tag>;
        }
      }
    },
    {
      title: "Ngày đặt",
      dataIndex: "createdAt",
      key: "createdAt",
      render: (value) => formatDateTime(value),
    },
    {
      title: "Thao tác",
      key: "actions",
      render: (record) => (
        <Space>
          <Link to={`/shop/orders/${record.id}`} >
            <Button type="link" icon={<EyeOutlined />}>
              Xem
            </Button>
          </Link>

          {record.status === "PENDING" && (
            <Button
              type="primary"
              icon={<CheckOutlined />}
              onClick={() => handleAction(record, "confirm")}
            >
              Xác nhận
            </Button>
          )}

          {record.status === "CONFIRMED" && (
            <Button
              color="blue"
              variant="outlined"
              icon={<CarOutlined />}
              onClick={() => handleAction(record, "delivering")}
            >
              Giao hàng
            </Button>
          )}

          {record.status === "DELIVERING" && (
            <Button
              color="green"
              variant="solid"
              icon={<CheckCircleOutlined />}
              onClick={() => handleAction(record, "delivered")}
            >
              Đã giao
            </Button>
          )}

          {/* {record.status === "DELIVERED" && (
            <Button
              color="green"
              variant="outlined"
              icon={<CheckCircleOutlined />}
              onClick={() => handleAction(record, "complete")}
            >
              Hoàn thành
            </Button>
          )} */}

          {(record.status === "PENDING" || record.status === "CONFIRMED") && (
            <Button
              danger
              icon={<CloseOutlined />}
              onClick={() => handleAction(record, "cancel")}
            >
              Hủy
            </Button>
          )}
        </Space>
      )
    },
  ];

  const handleAction = (record, action) => {
    let title = "";
    let content = "";
    let endpoint = "";
    let successMessage = "";

    switch (action) {
      case "confirm":
        title = "Xác nhận đơn hàng";
        content = `Bạn có chắc muốn xác nhận đơn ${record.orderCode}?`;
        endpoint = `shop/orders/${record.id}/confirm`;
        successMessage = "Xác nhận đơn hàng thành công";
        break;

      case "delivering":
        title = "Chuyển sang giao hàng";
        content = `Bạn có chắc muốn chuyển đơn ${record.orderCode} sang trạng thái đang giao?`;
        endpoint = `shop/orders/${record.id}/delivering`;
        successMessage = "Cập nhật trạng thái đang giao thành công";
        break;

      case "delivered":
        title = "Xác nhận đã giao";
        content = `Bạn có chắc muốn đánh dấu đơn ${record.orderCode} là đã giao?`;
        endpoint = `shop/orders/${record.id}/delivered`;
        successMessage = "Cập nhật trạng thái đã giao thành công";
        break;

      case "complete":
        title = "Hoàn thành đơn hàng";
        content = `Bạn có chắc muốn hoàn thành đơn ${record.orderCode}?`;
        endpoint = `shop/orders/${record.id}/complete`;
        successMessage = "Hoàn thành đơn hàng thành công";
        break;

      case "cancel":
        title = "Hủy đơn hàng";
        content = `Bạn có chắc muốn hủy đơn ${record.orderCode}?`;
        endpoint = `shop/orders/${record.id}/cancel`;
        successMessage = "Hủy đơn hàng thành công";
        break;

      default:
        return;
    }

    Modal.confirm({
      title,
      content,
      okText: "Xác nhận",
      cancelText: "Đóng",
      okButtonProps: {
        danger: action === "cancel",
      },
      onOk: async () => {
        try {
          const res = await patch(endpoint);
          const data = await res.json();

          if (!res.ok) {
            throw new Error(data.message || "Cập nhật trạng thái thất bại");
          }

          notification.success({
            title: "Thành công",
            description: successMessage,
          });

          onReload?.();
        } catch (error) {
          console.log();
          notification.error({
            title: "Thất bại",
            description: error.message || "Có lỗi xảy ra",
          });
        }
      },
    });
  }


  return (
    <>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={orders}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          pageSizeOptions: [5, 10, 20],
          showTotal: (value) => `Tổng ${value} đơn hàng`,
        }}
        onChange={(pagination) => {
          onChangePage?.(pagination.current, pagination.pageSize);
        }}
      />
    </>
  )
}

export default OrderTable;