import { Button, Form, Input, Modal, notification, Select, Space, Upload } from "antd";
import "./BannerEdit.scss";
import { useEffect, useState } from "react";
import { MdUpload } from "react-icons/md";
import { CgArrowsExchangeAlt } from "react-icons/cg";
import { useForm } from "antd/es/form/Form";
import { post, put } from "../../../../utils/request";


function BannerEdit({ openModal, record, onCancel, onReload }) {
  const [notificationApi, contextHolder] = notification.useNotification();
  const [form] = useForm();
  const [imageUrl, setImageUrl] = useState();

  useEffect(() => {
    if (record) {
      setImageUrl(record.imageUrl);
      form.setFieldsValue(record);
    } else {
      setImageUrl(null)
      form.resetFields();
    }
  }, [openModal, record])

  console.log(record);

  const uploadToCloudinary = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", "banners");
    try {
      const res = await fetch(`https://api.cloudinary.com/v1_1/dcjraarbb/image/upload`, {
        method: "POST",
        body: formData
      }
      );
      const data = await res.json();
      setImageUrl(data.secure_url);
    } catch (error) {
      console.log("Lỗi up ảnh đại diện: ", error);
    }
  };

  const handleUpload = (e) => {
    uploadToCloudinary(e.file);
  }

  const onFinish = e => {
    const newBanner = {
      ...e,
      imageUrl
    };

    console.log(newBanner);

    const fetchApi = async () => {
      try {
        let res = null;

        if (record) {
          res = await put(`admin/banners/${record.id}`, newBanner);
        } else {
          res = await post("admin/banners", newBanner);
        }

        if (!res.ok) {
          throw new Error("Error");
        }

        const data = await res.json();

        notificationApi.success({
          title: 'Thành công',
          description: record
            ? `Cập nhật Banner ${data.title} thành công`
            : `Thêm mới Banner ${data.title} thành công`
        })
        onReload();
      } catch (error) {
        console.log("Loi cap nhat / them Banner: ", error);
      } 
    }
    fetchApi();
  };


  return (
    <>
      {contextHolder}
      <Modal
        closable={{ 'aria-label': 'Custom Close Button' }}
        open={openModal}
        onCancel={onCancel}
        footer={null}
        style={{ top: '30px' }}
        forceRender
      >
        <div className="banner-edit">
          <div className="banner-edit__title">
            {record ? "Chỉnh sửa Banner" : "Thêm Banner mới"}
          </div>

          <Form
            form={form}
            initialValues={{ remember: true }}
            onFinish={onFinish}
            layout="vertical"
          >
            <Form.Item
              label="Tiêu đề"
              name="title"
              rules={[{ required: true, message: 'Vui lòng điền thông tin này!' }]}
            >
              <Input placeholder="Nhập tiêu đề banner..." />
            </Form.Item>

            <Form.Item
              label="Ảnh"
              name="imageUrl"
              rules={[{ required: true, message: 'Vui lòng thêm ảnh!' }]}
            >
              {imageUrl ? (
                <>
                  <img src={imageUrl} alt="" className="banner-edit__image" />
                  <Upload
                    showUploadList={false}
                    customRequest={handleUpload}
                  >
                    <Button color="primary" variant="dashed" icon={<CgArrowsExchangeAlt />}>Thay đổi ảnh</Button>
                  </Upload>
                </>
              ) : (
                <Upload
                  // showUploadList={false}
                  customRequest={handleUpload}
                >
                  <Button color="primary" variant="dashed" icon={<MdUpload />}>Tải ảnh lên</Button>
                </Upload>
              )}
            </Form.Item>

            <Form.Item
              label="Trạng thái"
              name="status"
              initialValue="INACTIVE"
            >
              <Select
                style={{ width: 180 }}
                options={[
                  { value: 'ACTIVE', label: 'Hiển thị' },
                  { value: 'INACTIVE', label: 'Ẩn' },
                ]}
              />
            </Form.Item>

            <Form.Item label={null}>
              <Space >
                <Button type="primary" htmlType="submit">
                  {record ? "Cập nhật" : "Thêm"}
                </Button>
                <Button onClick={onCancel} >
                  Hủy
                </Button>
              </Space>
            </Form.Item>
          </Form>

        </div>
      </Modal>
    </>
  )
}

export default BannerEdit;