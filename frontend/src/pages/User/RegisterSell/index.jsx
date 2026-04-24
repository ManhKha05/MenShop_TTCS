import { Button, Form, Input, InputNumber, notification, Upload } from "antd";
import "./RegisterSell.scss";
import { useState } from "react";
import { UploadOutlined } from '@ant-design/icons';
import { FaPhone } from "react-icons/fa6";
import { MdEmail } from "react-icons/md";
import { IoLocation } from "react-icons/io5";
import { useForm } from "antd/es/form/Form";
import { post } from "../../../utils/request";


function RegisterShop() {
  const [logo, setLogo] = useState("https://img.favpng.com/8/20/24/computer-icons-online-shopping-png-favpng-QuiWDXbsc69EE92m3bZ2i0ybS.jpg");
  const [form] = useForm();
  const [notificationApi, contextHolder] = notification.useNotification();

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
        const res = await post("shops", data);

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }

        notificationApi.success({
          title: "Gửi yêu cầu thành công",
          description: "Yêu cầu đăng ký mở Cửa hàng đã được gửi tới quản trị viên và sẽ gửi cho bạn kết quả sớm nhất!",
          placement: 'bottomRight'
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
      <div className="register-shop">
        <div className="register-shop__title">
          Mở Shop ngay hôm nay!
        </div>
        <div className="register-shop__subtitle">
          Chỉ mất 1 phút để tạo cửa hàng riêng của bạn. Điền thông tin dưới đây để sẵn sàng đăng bán những sản phẩm đầu tiên nhé.
        </div>
        <Form
          form={form}
          onFinish={onFinish}
        >
          <div className="register-shop__info">
            <div className="register-shop__image">
              <img src={logo} alt="avatar" style={{ height: '150px', display: 'block', marginBottom: '8px' }} />
              <Upload
                showUploadList={false}
                customRequest={handleUpload}
              >
                <Button icon={<UploadOutlined />}>Thay đổi ảnh</Button>
              </Upload>
            </div>

            <div className="register-shop__info__right">
              <div className="register-shop__detail">
                <div className="register-shop__label">
                  Tên cửa hàng
                </div>
                <Form.Item
                  name="name"
                  rules={[{ required: true, message: "Không được bỏ trống Tên cửa hàng!" }]}
                >
                  <Input className="register-shop__input" />
                </Form.Item>
              </div>

              <div className="register-shop__row">
                <div className="register-shop__detail">
                  <div className="register-shop__label">
                    Số điện thoại
                  </div>
                  <Form.Item
                    name="phone"
                    rules={[{ required: true, message: "Không được bỏ trống Số điện thoại!" }]}
                  >
                    <InputNumber className="register-shop__input" prefix={<FaPhone />} />
                  </Form.Item>
                </div>

                <div className="register-shop__detail">
                  <div className="register-shop__label">
                    Email liên hệ
                  </div>
                  <Form.Item
                    name="email"
                    rules={[{ required: true, message: "Không được bỏ trống Email!" }]}
                  >
                    <Input className="register-shop__input" prefix={<MdEmail />} />
                  </Form.Item>
                </div>
              </div>
            </div>
          </div>

          <div className="register-shop__body">
            <div className="register-shop__detail">
              <div className="register-shop__label">
                Địa chỉ
              </div>
              <Form.Item
                name="address"
                rules={[{ required: true, message: "Không được bỏ trống địa chỉ cửa hàng!" }]}
              >
                <Input className="register-shop__input" prefix={<IoLocation />} />
              </Form.Item>
            </div>

            <div className="register-shop__detail">
              <div className="register-shop__label">
                Mô tả cửa hàng
              </div>
              <Form.Item
                name="description"
                rules={[{ required: true, message: "Không được bỏ trống mô tả cửa hàng!" }]}
              >
                <Input.TextArea className="register-shop__input" />
              </Form.Item>
            </div>
          </div>

          <div className="register-shop__action">
            {/* <button className="register-shop__btn register-shop__btn--default" type="button" onClick={handleCancel}>
              Hủy
            </button> */}
            <button className="register-shop__btn register-shop__btn--primary" type="submit">
              Gửi yêu cầu
            </button>
          </div>
        </Form>
      </div>
    </>
  )
}


export default RegisterShop;