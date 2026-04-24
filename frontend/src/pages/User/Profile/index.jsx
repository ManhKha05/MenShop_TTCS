import { Button, DatePicker, Form, Input, notification, Radio, Upload } from "antd";
import "./Profile.scss"
import { PlusOutlined } from '@ant-design/icons';
import { useForm } from "antd/es/form/Form";
import { useEffect, useState } from "react";
import { get, put } from "../../../utils/request";

function Profile() {
  const [form] = useForm();
  const [notificationApi, contextHolder] = notification.useNotification();
  const [avatar, setAvatar] = useState();

  useEffect(() => {
    const fetchApi = async () => {
      try {
        const res = await get("users/me");

        const data = await res.json();
        form.setFieldsValue(data);
        setAvatar(data.avatar);
        console.log(data);
      } catch (error) {
        console.error(error);
      }
    }
    fetchApi();
  }, [])

  const uploadToCloudinary = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", "usersAvt");
    try {
      const res = await fetch(`https://api.cloudinary.com/v1_1/dcjraarbb/image/upload`, {
        method: "POST",
        body: formData
      }
      );
      const data = await res.json();
      setAvatar(data.secure_url);
    } catch (error) {
      console.log("Lỗi up ảnh đại diện: ", error);
    }
  };

  const handleUpload = (e) => {
    uploadToCloudinary(e.file);
  }

  const onFinish = values => {
    const data = {
      ...values,
      avatar
    }
    const fetchApi = async () => {
      try {
        const res = await put("users/me", data);

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        notificationApi.success({
          title: "Cập nhật hồ sơ cá nhân thành công"
        })

      } catch (error) {
        console.error(error);
      }
    }

    fetchApi();
  };

  return (
    <>
      {contextHolder}
      <div className="profile">
        <div className="profile__header">
          <div className="profile__header__title">
            Hồ sơ của tôi
          </div>
          <div className="profile__header__subtitle">
            Quản lí thông tin hồ sơ để bảo mật tài khoản
          </div>
        </div>

        <div className="profile__body">
          <Form
            form={form}
            wrapperCol={{ span: 16 }}
            labelCol={{ span: 6 }}
            style={{ width: 600 }}
            onFinish={onFinish}
            className="profile__form"
          >
            <Form.Item
              label="Ảnh đại diện"
              name="avatar"
            >
              <div style={{ display: 'flex' }}>
                <img src={avatar} alt="" className="profile__avt" />
                <Upload
                  listType="picture-card"
                  className="profile__avt-upload"
                  showUploadList={false}
                  customRequest={handleUpload}
                >
                  <button
                    style={{ border: 0, background: 'none' }}
                    type="button"
                  >
                    <PlusOutlined />
                    <div style={{ marginTop: 8 }}>Chọn ảnh</div>
                  </button>
                </Upload>
              </div>

            </Form.Item>

            <Form.Item
              label="Email"
              name="email"
            >
              {/* <Input /> */}
              <div className="ant-input">
                {localStorage.getItem("email")}
              </div>
            </Form.Item>

            {/* <Form.Item
              label="Password"
              name="password"
            >
              <Input.Password />
            </Form.Item> */}

            <Form.Item
              label="Họ và tên"
              name="fullName"
            >
              <Input />
            </Form.Item>

            {/* <Form.Item
              label="Email"
              name="email"
            >
              <Input />
            </Form.Item> */}

            <Form.Item
              label="Số điện thoại"
              name="phone"
            >
              <Input />
            </Form.Item>

            <Form.Item
              label="Giới tính"
              name="gender"
            >
              <Radio.Group>
                <Radio value="MALE">Nam</Radio>
                <Radio value="FEMALE">Nữ</Radio>
                <Radio value="OTHER">Khác</Radio>
              </Radio.Group>
            </Form.Item>

            {/* <Form.Item
              label="Ngày sinh"
              name="birthday"
            >
              <DatePicker
                placeholder="Chọn ngày sinh"
                format="DD-MM-YYYY"
              />
            </Form.Item> */}

            <Form.Item label={null}>
              <Button type="primary" htmlType="submit">
                Lưu
              </Button>
            </Form.Item>
          </Form>

          <div className="profile__body__avatar">

          </div>
        </div>
      </div>
    </>
  )
}

export default Profile;