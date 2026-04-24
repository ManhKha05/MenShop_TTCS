import "./ReviewModal.scss";
import { Button, Form, Input, Modal, Rate, Upload, notification } from "antd";
import { useState } from "react";
import { post } from "../../../utils/request";

const ReviewModal = ({ open, onCancel, orderId, product, onSuccess }) => {
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState([]);
  const [loading, setLoading] = useState(false);

  const uploadImageToCloudinary = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", "ReviewImage");

    const res = await fetch("https://api.cloudinary.com/v1_1/dcjraarbb/image/upload", {
      method: "POST",
      body: formData,
    });

    const data = await res.json();

    if (!res.ok) {
      throw new Error(data?.error?.message || "Upload ảnh thất bại");
    }

    return data.secure_url;
  };

  const handleBeforeUpload = async (file) => {
    // const isValidType = ["image/jpeg", "image/png", "image/webp", "image/jpg"].includes(file.type);
    // if (!isValidType) {
    //   notification.error({
    //     message: "File không hợp lệ",
    //     description: "Chỉ chấp nhận JPG, JPEG, PNG, WEBP",
    //   });
    //   return Upload.LIST_IGNORE;
    // }

    // const isLt5M = file.size / 1024 / 1024 < 5;
    // if (!isLt5M) {
    //   notification.error({
    //     message: "Ảnh quá lớn",
    //     description: "Ảnh phải nhỏ hơn 5MB",
    //   });
    //   return Upload.LIST_IGNORE;
    // }

    const tempUid = file.uid;

    setFileList((prev) => [
      ...prev,
      {
        uid: tempUid,
        name: file.name,
        status: "uploading",
      },
    ]);

    try {
      const url = await uploadImageToCloudinary(file);

      setFileList((prev) =>
        prev.map((item) =>
          item.uid === tempUid
            ? {
              ...item,
              status: "done",
              url,
            }
            : item
        )
      );
    } catch (error) {
      setFileList((prev) => prev.filter((item) => item.uid !== tempUid));
      notification.error({
        message: "Upload thất bại",
        description: error.message || "Không thể upload ảnh",
      });
    }

    return Upload.LIST_IGNORE;
  };

  const handleRemove = (file) => {
    setFileList((prev) => prev.filter((item) => item.uid !== file.uid));
  };

  const handleSubmit = async (values) => {
    try {
      const stillUploading = fileList.some((file) => file.status === "uploading");
      if (stillUploading) {
        notification.warning({
          message: "Ảnh đang tải lên",
          description: "Vui lòng chờ upload ảnh xong rồi gửi đánh giá",
        });
        return;
      }

      setLoading(true);

      const imageUrls = fileList
        .filter((file) => file.status === "done" && file.url)
        .map((file) => file.url);

      const res = await post("reviews", {
        orderId,
        productId: product.id,
        rating: values.rating,
        content: values.content,
        images: imageUrls,
      });

      if (!res.ok) {
        const err = await res.json();
        throw new Error(err?.message || "Không thể gửi đánh giá");
      }

      notification.success({
        message: "Thành công",
        description: "Đánh giá sản phẩm thành công",
      });

      form.resetFields();
      setFileList([]);
      onSuccess?.();
      onCancel?.();
    } catch (error) {
      notification.error({
        message: "Thất bại",
        description: error.message || "Không thể gửi đánh giá",
      });
    } finally {
      setLoading(false);
    }
  };

  const isUploading = fileList.some((file) => file.status === "uploading");

  const handleClose = () => {
    form.resetFields();      // reset form
    setFileList([]);         // xóa ảnh đã upload
    onCancel?.();            // đóng modal
  };

  return (
    <Modal
      open={open}
      onCancel={handleClose}
      footer={null}
      title={null}
      destroyOnClose
      centered
      className="review-modal"
      width={720}
    >
      <div className="review-modal__header">
        <h2 className="review-modal__title">Đánh giá sản phẩm</h2>
        <p className="review-modal__subtitle">Chia sẻ cảm nhận thực tế của bạn về sản phẩm</p>
      </div>

      <div className="review-modal__product">
        <div className="review-modal__product-image-wrap">
          <img
            src={product?.image}
            alt={product?.name}
            className="review-modal__product-image"
          />
        </div>
        <div className="review-modal__product-info">
          <div className="review-modal__product-label">Sản phẩm</div>
          <div className="review-modal__product-name">{product?.name}</div>
        </div>
      </div>

      <Form form={form} layout="vertical" onFinish={handleSubmit} className="review-modal__form">
        <Form.Item label="Hình ảnh thực tế" className="review-modal__form-item">
          <Upload
            listType="picture-card"
            fileList={fileList}
            beforeUpload={handleBeforeUpload}
            onRemove={handleRemove}
            onPreview={(file) => file.url && window.open(file.url, "_blank")}
            multiple
            maxCount={5}
            className="review-modal__upload"
          >
            {fileList.length < 5 && (
              <div className="review-modal__upload-trigger">
                <span className="review-modal__upload-plus">+</span>
                <span>Tải ảnh</span>
              </div>
            )}
          </Upload>
        </Form.Item>

        <Form.Item
          label="Số sao"
          name="rating"
          rules={[{ required: true, message: "Vui lòng chọn số sao" }]}
          className="review-modal__form-item"
        >
          <Rate className="review-modal__rate" />
        </Form.Item>

        <Form.Item
          label="Nội dung đánh giá"
          name="content"
          rules={[{ required: true, message: "Vui lòng nhập nội dung đánh giá" }]}
          className="review-modal__form-item"
        >
          <Input.TextArea
            rows={3}
            placeholder="Hãy chia sẻ cảm nhận thật của bạn về chất liệu, form dáng, độ hoàn thiện, tốc độ giao hàng..."
            className="review-modal__textarea"
          />
        </Form.Item>

        <div className="review-modal__actions">
          <Button onClick={handleClose} className="review-modal__btn review-modal__btn--ghost">
            Hủy
          </Button>
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            disabled={isUploading}
            className="review-modal__btn review-modal__btn--primary"
          >
            Gửi đánh giá
          </Button>
        </div>
      </Form>
    </Modal>
  );
};

export default ReviewModal;