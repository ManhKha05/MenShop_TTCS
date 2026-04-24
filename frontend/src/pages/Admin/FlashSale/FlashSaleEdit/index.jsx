import { Button, Flex, Form, Input, Modal, notification, Space } from "antd";
import "./FlashSaleEdit.scss";
import { useForm } from "antd/es/form/Form";
import { MdDriveFileRenameOutline } from "react-icons/md";
import { DatePicker } from 'antd';
import { useEffect } from "react";
import { post, put } from "../../../../utils/request";
import dayjs from "dayjs";

function FlashSaleEdit({ open, onReload, onCancel, record }) {
  const [form] = useForm();
  const [notificationApi, contextHolder] = notification.useNotification();
  // console.log(record);

  useEffect(() => {
    if (record) {
      form.setFieldsValue({
        ...record,
        startTime: dayjs(record.startTime, "YYYY-MM-DDTHH:mm:ss"),
        endTime: dayjs(record.endTime, "YYYY-MM-DDTHH:mm:ss")
      });
    } else {
      form.resetFields();
    }
  }, [open, record])


  const onFinish = async (values) => {
    const payload = {
      ...values,
      startTime: values.startTime.format("YYYY-MM-DDTHH:mm:ss"),
      endTime: values.endTime.format("YYYY-MM-DDTHH:mm:ss")
    }
    try {
      let res = null;

      if (record) {
        res = await put(`admin/flash-sales/${record.id}`, payload);
      } else {
        res = await post('admin/flash-sales', payload);
      }

      if (!res.ok) {
        const err = await res.json();
        throw new Error(err.message);
      }

      notificationApi.success({
        title: (record ? "Cập nhật " : "Tạo ") + "Flash Sale thành công",
        description: record ? "Thông tin Flash Sale đã được cập nhật." : "Chương trình Flash Sale đã được tạo và sẽ hiển thị theo thời gian đã thiết lập."
      })
      onReload();
    } catch (error) {
      notificationApi.error({
        title: 'Lỗi',
        description: error.message || 'Có lỗi xảy ra'
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
        forceRender
      >
        <div className="flashsale-edit">
          <div className="title">
            {record ? "Chỉnh sửa Flash Sale" : "Thiết lập Flash Sale"}
          </div>
          <Form
            form={form}
            name="basic"
            onFinish={onFinish}
            layout="vertical"
          >
            <Form.Item
              label="TÊN CHƯƠNG TRÌNH"
              name="name"
              rules={[{ required: true, message: 'Vui lòng điền tên chương trình!' }]}
            >
              <Input prefix={<MdDriveFileRenameOutline />} placeholder="Ví dụ: Siêu Sale Mùa Hè 2026" />
            </Form.Item>

            <Form.Item
              label="MÔ TẢ"
              name="description"
              rules={[{ required: true, message: 'Vui lòng điền mô tả cho chương trình!' }]}
            >
              <Input.TextArea placeholder="Nhập nội dung mô tả chiến dịch" />
            </Form.Item>

            <Flex justify="space-between">
              <Form.Item
                label="THỜI GIAN BẮT ĐẦU"
                name="startTime"
                rules={[{ required: true, message: 'Vui lòng chọn thời gian bắt đầu!' }]}
              >
                <DatePicker
                  showTime
                  format="HH:mm:ss DD-MM-YYYY"
                  placeholder="Chọn thời gian bắt đầu"
                  disabledDate={(current) => {
                    return current && current < dayjs().startOf('day');
                  }}

                  // ❌ Không cho chọn giờ phút giây trước hiện tại (nếu là hôm nay)
                  disabledTime={(current) => {
                    if (!current) return {};

                    const now = dayjs();

                    // Nếu chọn đúng hôm nay
                    if (current.isSame(now, 'day')) {
                      return {
                        disabledHours: () =>
                          Array.from({ length: now.hour() }, (_, i) => i),

                        disabledMinutes: (selectedHour) => {
                          if (selectedHour === now.hour()) {
                            return Array.from({ length: now.minute() }, (_, i) => i);
                          }
                          return [];
                        },

                        disabledSeconds: (selectedHour, selectedMinute) => {
                          if (
                            selectedHour === now.hour() &&
                            selectedMinute === now.minute()
                          ) {
                            return Array.from({ length: now.second() }, (_, i) => i);
                          }
                          return [];
                        }
                      };
                    }

                    return {};
                  }}
                />
              </Form.Item>

              <Form.Item
                label="THỜI GIAN KẾT THÚC"
                name="endTime"
                dependencies={['startTime']}
                rules={[
                  { required: true, message: 'Vui lòng chọn thời gian kết thúc!' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      const startTime = getFieldValue('startTime');

                      if (!value || !startTime) return Promise.resolve();

                      if (value.isAfter(startTime)) {
                        return Promise.resolve();
                      }

                      return Promise.reject(
                        new Error('Thời gian kết thúc phải sau bắt đầu!')
                      );
                    }
                  })
                ]}
              >
                <DatePicker
                  showTime
                  format="HH:mm:ss DD-MM-YYYY"
                  placeholder="Chọn thời gian kết thúc"
                  disabledDate={(current) => {
                    const startTime = form.getFieldValue('startTime');
                    if (!startTime) return false;

                    return current && current < startTime.startOf('day');
                  }}
                  disabledTime={(current) => {
                    const startTime = form.getFieldValue('startTime');
                    if (!current || !startTime) return {};

                    if (current.isSame(startTime, 'day')) {
                      return {
                        disabledHours: () =>
                          Array.from({ length: startTime.hour() }, (_, i) => i),

                        disabledMinutes: (selectedHour) => {
                          if (selectedHour === startTime.hour()) {
                            return Array.from(
                              { length: startTime.minute() },
                              (_, i) => i
                            );
                          }
                          return [];
                        },

                        disabledSeconds: (selectedHour, selectedMinute) => {
                          if (
                            selectedHour === startTime.hour() &&
                            selectedMinute === startTime.minute()
                          ) {
                            return Array.from(
                              { length: startTime.second() },
                              (_, i) => i
                            );
                          }
                          return [];
                        }
                      };
                    }

                    return {};
                  }}
                />
              </Form.Item>

            </Flex>


            <Form.Item label={null}>
              <Space>
                <Button type="primary" htmlType="submit">
                  Lưu chương trình
                </Button>
                <Button onClick={onCancel}>
                  Hủy bỏ
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </div>
      </Modal>
    </>
  )
}

export default FlashSaleEdit;