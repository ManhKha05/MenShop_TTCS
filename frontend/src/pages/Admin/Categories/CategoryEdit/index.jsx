import "./CategoryEdit.scss";
import { Button, Form, Input, Modal, notification, Select, Upload } from "antd";
import { useForm } from "antd/es/form/Form";
import { UploadOutlined } from '@ant-design/icons';
import { useEffect, useState } from "react";
import { post, put } from "../../../../utils/request";


function CategoryEdit({ parents, category, open, onCancel, onReload }) {
  const [form] = useForm();
  const [notificationApi, contextHolder] = notification.useNotification();
  const [imageUrl, setImageUrl] = useState();

  useEffect(() => {
    if (category) {
      form.setFieldsValue(category);
      setImageUrl(category.imageUrl);
    } else {
      form.resetFields();
      setImageUrl(null)
    }
  }, [open, category, form])

  const uploadToCloudinary = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", "categories");
    try {
      const res = await fetch(`https://api.cloudinary.com/v1_1/dcjraarbb/image/upload`, {
        method: "POST",
        body: formData
      }
      );
      const data = await res.json();
      setImageUrl(data.secure_url);
    } catch (error) {
      console.log("Lỗi up ảnh danh mục: ", error);
    }
  };

  const handleUpload = (e) => {
    uploadToCloudinary(e.file);
  }

  const onFinish = (e) => {
    const newCategory = {
      ...e,
      imageUrl: imageUrl
    }
    console.log(newCategory);
    const fetchApi = async () => {
      try {
        let res = null;

        if (category) {
          res = await put(`admin/categories/${category.id}`, newCategory);
        } else {
          res = await post("admin/categories", newCategory);
        }

        if (!res.ok) {
          throw new Error("Loi");
        }

        const data = await res.json();

        onReload();
        notificationApi.success({
          title: "Thành công",
          description: (category ?  "Cập nhật " : "Thêm ") + `danh mục ${data.name} thành công!`
        })

      } catch (error) {
        console.log("Loi Category Edit: ", error);
      }
    }
    fetchApi();
  }
  return (
    <>
      {contextHolder}
      <Modal
        open={open}
        onCancel={onCancel}
        footer={null}
        style={{ top: '30px' }}
      >
        <div className="category-edit">
          <h2 style={{ marginTop: '10px' }}>{!category ? "Thêm danh mục" : "Chỉnh sửa danh mục"}</h2>
          <Form
            layout="vertical"
            onFinish={onFinish}
            form={form}
          // size="large"
          >
            <Form.Item
              label="Tên danh mục"
              name="name"
              rules={[{ required: true, message: 'Vui lòng nhập tên danh mục!' }]}
            >
              <Input placeholder="Nhập tên danh mục" />
            </Form.Item>

            <Form.Item
              label="Danh mục cha (Không chọn thì đây là danh mục cha)"
              name="parentId"
            >
              <Select
                allowClear
                style={{ width: 180 }}
                placeholder="Chọn danh mục cha"
                options={[
                  ...parents.map(c => ({
                    value: c.id, label: c.name
                  }))
                ]}
              />
            </Form.Item>

            <Form.Item
              label="Ảnh danh mục"
              name="imageUrl"
              rules={[{ required: true, message: 'Vui lòng thêm ảnh !' }]}
            >
              {imageUrl ? (
                <>
                  <img src={imageUrl} alt="avatar" style={{ height: '150px', display: 'block', marginBottom: '8px' }} />
                  <Upload
                    showUploadList={false}
                    customRequest={handleUpload}
                  >
                    <Button icon={<UploadOutlined />}>Thay đổi ảnh</Button>
                  </Upload>
                </>
              ) : (
                <Upload
                  listType="picture-card"
                  // showUploadList={false}
                  customRequest={handleUpload}
                >
                  + Thêm ảnh
                </Upload>
              )}
            </Form.Item>

            <Form.Item
              label="Trạng thái"
              name="status"
              rules={[{ required: true, message: 'Vui lòng chọn trạng thái!' }]}
            // initialValue="ACTIVE"
            >
              <Select
                // defaultValue="lucy"
                style={{ width: 160 }}
                placeholder="Chọn trạng thái"
                options={[
                  { value: 'ACTIVE', label: 'Hiển thị' },
                  { value: 'INACTIVE', label: 'Ẩn' }
                ]}
              />
            </Form.Item>

            <Form.Item style={{ textAlign: "right" }}>
              <Button type="primary" htmlType="submit">
                {!category ? "Tạo danh mục" : "Cập nhật"}
              </Button>
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  )
}

export default CategoryEdit;