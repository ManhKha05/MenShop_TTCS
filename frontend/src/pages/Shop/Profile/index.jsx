import { Button, Form, Input, InputNumber, notification, Upload } from "antd";
import "./Profile.scss";
import { useEffect, useState } from "react";
import { UploadOutlined } from '@ant-design/icons';
import { FaPhone } from "react-icons/fa6";
import { MdEmail } from "react-icons/md";
import { IoLocation } from "react-icons/io5";
import { useForm } from "antd/es/form/Form";
import { get, put } from "../../../utils/request";


function Profile() {
  const [logo, setLogo] = useState();
  const [form] = useForm();
  const [notificationApi, contextHolder] = notification.useNotification();

  useEffect(() => {
    const fetchApi = async () => {
      try {
        const res = await get("shops/profile");

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        const shop = await res.json();
        console.log(shop);

        form.setFieldsValue(shop);
        setLogo(shop.logo);

      } catch (error) {
        console.error(error);
      }
    }
    fetchApi();
  }, [])

  const uploadToCloudinary = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", "shopsAvt");
    try {
      const res = await fetch(`https://api.cloudinary.com/v1_1/dcjraarbb/image/upload`, {
        method: "POST",
        body: formData
      }
      );
      const data = await res.json();
      setLogo(data.secure_url);
    } catch (error) {
      console.log("Lỗi up ảnh đại diện: ", error);
    }
  };

  const handleUpload = (e) => {
    uploadToCloudinary(e.file);
  }

  const handleCancel = () => {
    console.log("cancel");
  }

  const onFinish = values => {
    const data = {
      ...values,
      logo
    }

    const fetchApi = async () => {
      try {
        const res = await put("shops/profile", data);

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        notificationApi.success({
          title: "Cập nhật thông tin cửa hàng thành công",
        })

      } catch (error) {
        console.log(error);
      }
    }
    fetchApi();
  };

  return (
    <>
      {contextHolder}
      <div className="profile-shop">
        <div className="profile-shop__title">
          Hồ sơ Cửa hàng
        </div>
        <div className="profile-shop__subtitle">
          Cập nhật thông tin chi tiết về cửa hàng của bạn để khách hàng dễ dàng tìm kiếm.
        </div>
        <Form
          form={form}
          onFinish={onFinish}
        >
          <div className="profile-shop__info">
            <div className="profile-shop__image">
              <img src={logo} alt="avatar" style={{ height: '150px', display: 'block', marginBottom: '8px' }} />
              <Upload
                showUploadList={false}
                customRequest={handleUpload}
              >
                <Button icon={<UploadOutlined />}>Thay đổi ảnh</Button>
              </Upload>
            </div>

            <div className="profile-shop__info__right">
              <div className="profile-shop__detail">
                <div className="profile-shop__label">
                  Tên cửa hàng
                </div>
                <Form.Item
                  name="name"
                  rules={[{ required: true, message: "Không được bỏ trống Tên cửa hàng!" }]}
                >
                  <Input className="profile-shop__input" />
                </Form.Item>
              </div>

              <div className="profile-shop__row">
                <div className="profile-shop__detail">
                  <div className="profile-shop__label">
                    Số điện thoại
                  </div>
                  <Form.Item
                    name="phone"
                    rules={[{ required: true, message: "Không được bỏ trống Số điện thoại!" }]}
                  >
                    <InputNumber className="profile-shop__input" prefix={<FaPhone />} />
                  </Form.Item>
                </div>

                <div className="profile-shop__detail">
                  <div className="profile-shop__label">
                    Email liên hệ
                  </div>
                  <Form.Item
                    name="email"
                    rules={[{ required: true, message: "Không được bỏ trống Email!" }]}
                  >
                    <Input className="profile-shop__input" prefix={<MdEmail />} />
                  </Form.Item>
                </div>
              </div>
            </div>
          </div>

          <div className="profile-shop__body">
            <div className="profile-shop__detail">
              <div className="profile-shop__label">
                Địa chỉ
              </div>
              <Form.Item
                name="address"
                rules={[{ required: true, message: "Không được bỏ trống Số điện thoại!" }]}
              >
                <Input className="profile-shop__input" prefix={<IoLocation />} />
              </Form.Item>
            </div>

            <div className="profile-shop__detail">
              <div className="profile-shop__label">
                Mô tả cửa hàng
              </div>
              <Form.Item
                name="description"
                rules={[{ required: true, message: "Không được bỏ trống Số điện thoại!" }]}
              >
                <Input.TextArea className="profile-shop__input" />
              </Form.Item>
            </div>
          </div>

          <div className="profile-shop__action">
            {/* <button className="profile-shop__btn profile-shop__btn--default" type="button" onClick={handleCancel}>
              Hủy
            </button> */}
            <button className="profile-shop__btn profile-shop__btn--primary" type="submit">
              Lưu thay đổi
            </button>
          </div>
        </Form>
      </div>
    </>
  )
}

export default Profile;