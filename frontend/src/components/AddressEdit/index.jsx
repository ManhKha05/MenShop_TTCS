import { Button, Form, Input, Modal, notification } from "antd";
import "./AddressEdit.scss";
import { useEffect } from "react";
import { useForm } from "antd/es/form/Form";
import { post, put } from "../../utils/request";


function AddressEdit({ open, onCancel, record, onReload, onSuccess }) {
  const [form] = useForm();
  const [notificationApi, contextHolder] = notification.useNotification();

  useEffect(() => {
    if (record) {
      form.setFieldsValue(record);
    } else {
      form.resetFields();
    }
  }, [open, record, form]);

  const onFinish = values => {
    const fetchApi = async () => {
      try {
        let res = null;

        if (record) {
          res = await put(`addresses/${record.id}`, values);
        } else {
          res = await post("addresses", values);
        }

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        notificationApi.success({
          title: (record ? "Chỉnh sửa " : "Thêm ") + "địa chỉ thành công!"
        })
        onCancel();
        if(onReload) onReload();
        onSuccess();
      } catch (error) {
        console.error(error);
      }
    }
    fetchApi();
  };

  return (
    <>
      {contextHolder}
      <Modal
        open={open}
        onCancel={onCancel}
        footer={null}
      >
        <div className="address-edit">
          <div className="title">
            {record ? "Chỉnh sửa địa chỉ" : "Thêm địa chỉ mới"}
          </div>

          <Form
            onFinish={onFinish}
            layout="vertical"
            form={form}
          >
            <Form.Item
              label="Tên người nhận"
              name="receiverName"
              rules={[{ required: true, message: 'Vui lòng điền tên người nhận!' }]}
            >
              <Input />
            </Form.Item>

            <Form.Item
              label="Số điện thoại"
              name="phone"
              rules={[{ required: true, message: 'Vui lòng điền số điện thoại!' }]}
            >
              <Input />
            </Form.Item>

            <Form.Item
              label="Địa chỉ nhận hàng"
              name="address"
              rules={[{ required: true, message: 'Vui lòng điền địa chỉ!' }]}
            >
              <Input />
            </Form.Item>

            <Form.Item label={null}>
              <Button type="primary" htmlType="submit">
                {record ? "Cập nhật" : "Thêm"}
              </Button>
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  )
}

export default AddressEdit;