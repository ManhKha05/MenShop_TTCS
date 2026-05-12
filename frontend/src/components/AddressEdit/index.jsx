import { Button, Form, Input, Modal, notification, Select } from "antd";
import "./AddressEdit.scss";
import { useEffect, useState } from "react";
import { useForm } from "antd/es/form/Form";
import { get, post, put } from "../../utils/request";


function AddressEdit({ open, onCancel, record, onReload, onSuccess }) {
  const [form] = useForm();
  const [notificationApi, contextHolder] = notification.useNotification();

  const [provinces, setProvinces] = useState([]);
  const [districts, setDistricts] = useState([]);
  const [wards, setWards] = useState([]);

  useEffect(() => {
    if (open) {
      loadProvinces();

      if (record) {
        form.setFieldsValue({
          receiverName: record.receiverName,
          phone: record.phone,
          detailAddress: record.detailAddress,
          provinceId: record.provinceId,
          districtId: record.districtId,
          wardId: record.wardId,
          isDefault: record.isDefault,
        });

        if (record.provinceId) loadDistricts(record.provinceId);
        if (record.districtId) loadWards(record.districtId);
      } else {
        form.resetFields();
        setDistricts([]);
        setWards([]);
      }
    }
  }, [open, record]);

  const loadProvinces = async () => {
    const res = await get("addresses/provinces");
    const data = await res.json();
    setProvinces(data);
  };

  const loadDistricts = async (provinceId) => {
    const res = await get(`addresses/districts?provinceId=${provinceId}`);
    const data = await res.json();
    setDistricts(data);
  };

  const loadWards = async (districtId) => {
    const res = await get(`addresses/wards?districtId=${districtId}`);
    const data = await res.json();
    setWards(data);
  };

  const handleProvinceChange = (provinceId) => {
    form.setFieldsValue({
      districtId: null,
      wardId: null,
    });

    setDistricts([]);
    setWards([]);
    loadDistricts(provinceId);
  };

  const handleDistrictChange = (districtId) => {
    form.setFieldsValue({
      wardId: null,
    });

    setWards([]);
    loadWards(districtId);
  };

  const onFinish = async (values) => {
    try {
      // const province = provinces.find(item => item.id === values.provinceId);
      // const district = districts.find(item => item.id === values.districtId);
      // const ward = wards.find(item => item.id === values.wardId);

      // const payload = {
      //   ...values,
      //   address: `${values.detailAddress}, ${ward?.name}, ${district?.name}, ${province?.name}`,
      // };

      let res;

      console.log(values);

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
        message: record ? "Cập nhật địa chỉ thành công!" : "Thêm địa chỉ thành công!",
      });

      onCancel();
      if (onReload) onReload();
      if (onSuccess) onSuccess();

    } catch (error) {
      notificationApi.error({
        message: "Lỗi",
        description: error.message,
      });
    }
  };

  return (
    <>
      {contextHolder}
      <Modal
        open={open}
        onCancel={onCancel}
        footer={null}
        centered
        width={760}
        className="address-modal"
      >
        <div className="address-edit">
          <div className="address-edit__header">
            <h2>{record ? "Chỉnh sửa địa chỉ" : "Thêm địa chỉ mới"}</h2>
            <p>Thông tin này dùng để giao hàng và tính phí vận chuyển.</p>
          </div>

          <Form form={form} onFinish={onFinish} layout="vertical">
            <div className="address-edit__row address-edit__row--2">
              <Form.Item
                label="Tên người nhận"
                name="receiverName"
                rules={[{ required: true, message: "Vui lòng điền tên người nhận!" }]}
              >
                <Input placeholder="Nhập tên người nhận" />
              </Form.Item>

              <Form.Item
                label="Số điện thoại"
                name="phone"
                rules={[
                  { required: true, message: "Vui lòng điền số điện thoại!" },
                  { pattern: /^(0[0-9]{9})$/, message: "Số điện thoại không hợp lệ!" },
                ]}
              >
                <Input placeholder="Nhập số điện thoại" />
              </Form.Item>
            </div>

            <div className="address-edit__row address-edit__row--3">
              <Form.Item
                label="Tỉnh / Thành phố"
                name="provinceId"
                rules={[{ required: true, message: "Vui lòng chọn tỉnh/thành phố!" }]}
              >
                <Select
                  placeholder="Chọn tỉnh/thành phố"
                  onChange={handleProvinceChange}
                  showSearch
                  optionFilterProp="label"
                  options={provinces.map(item => ({
                    value: item.id,
                    label: item.name,
                  }))}
                />
              </Form.Item>

              <Form.Item
                label="Quận / Huyện"
                name="districtId"
                rules={[{ required: true, message: "Vui lòng chọn quận/huyện!" }]}
              >
                <Select
                  placeholder="Chọn quận/huyện"
                  onChange={handleDistrictChange}
                  disabled={!form.getFieldValue("provinceId")}
                  showSearch
                  optionFilterProp="label"
                  options={districts.map(item => ({
                    value: item.id,
                    label: item.name,
                  }))}
                />
              </Form.Item>

              <Form.Item
                label="Phường / Xã"
                name="wardId"
                rules={[{ required: true, message: "Vui lòng chọn phường/xã!" }]}
              >
                <Select
                  placeholder="Chọn phường/xã"
                  disabled={!form.getFieldValue("districtId")}
                  showSearch
                  optionFilterProp="label"
                  options={wards.map(item => ({
                    value: item.id,
                    label: item.name,
                  }))}
                />
              </Form.Item>
            </div>

            <Form.Item
              label="Địa chỉ cụ thể"
              name="detailAddress"
              rules={[{ required: true, message: "Vui lòng nhập địa chỉ cụ thể!" }]}
            >
              <Input placeholder="Ví dụ: Số 12, ngõ 5, đường ABC" />
            </Form.Item>

            <div className="address-edit__actions">
              <Button onClick={onCancel}>Hủy</Button>
              <Button type="primary" htmlType="submit">
                {record ? "Cập nhật địa chỉ" : "Thêm địa chỉ"}
              </Button>
            </div>
          </Form>
        </div>
      </Modal>
    </>
  )
}

export default AddressEdit;