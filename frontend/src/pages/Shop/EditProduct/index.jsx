import { useEffect, useState } from "react";
import "./EditProduct.scss";
import { MdOutlineImage } from "react-icons/md";
import { Button, Col, Form, Input, InputNumber, notification, Row, Select, Upload } from "antd";
import { BsInfoSquare } from "react-icons/bs";
import { TiPlusOutline } from "react-icons/ti";
import TextArea from "antd/es/input/TextArea";
import { BsLayers } from 'react-icons/bs';
import { FiPlusCircle, FiTrash2 } from 'react-icons/fi';
import { BsTags } from 'react-icons/bs';
import { MdLocalShipping } from "react-icons/md";
import { get, post, put } from "../../../utils/request";
import { useParams, useNavigate } from "react-router-dom";


function EditProduct() {
  const [api, contextHolder] = notification.useNotification();
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;
  const [imageList, setImageList] = useState([]);
  const [form] = Form.useForm();
  const [categories, setCategories] = useState([]);
  const [variants, setVariants] = useState([]);
  const [attributes, setAttributes] = useState([]);


  useEffect(() => {
    const fetchApi = async () => {
      try {
        const [categoriesRes, productRes] = await Promise.all([
          get("shop/categories"),
          isEdit ? get(`shop/products/${id}`) : null
        ]);

        const categoriesData = await categoriesRes.json();
        setCategories(categoriesData);

        if (isEdit && productRes) {
          const productData = await productRes.json();

          console.log(productData);

          form.setFieldsValue({
            name: productData.name,
            brandName: productData.brandName,
            price: productData.price,
            sale_price: productData.salePrice,
            description: productData.description,
            weight: productData.weight,
            length: productData.length,
            width: productData.width,
            height: productData.height,
            category: productData.categoryId,
            status: productData.status
          });

          setImageList(productData.images || []);

          setVariants(productData.variants || []);

          const attrArray = Object.entries(productData.attributesJson || {}).map(
            ([key, value], index) => ({
              id: index,
              name: key,
              value: value
            })
          );
          setAttributes(attrArray);
        }

      } catch (error) {
        console.log(error);
      }
    };

    fetchApi();
  }, [id]);


  const uploadToCloudinary = async (file) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("upload_preset", "productsImg");

    try {
      const res = await fetch(
        "https://api.cloudinary.com/v1_1/dcjraarbb/image/upload",
        {
          method: "POST",
          body: formData,
        }
      );

      const data = await res.json();

      return {
        uid: file.uid,
        name: file.name,
        status: "done",
        url: data.secure_url,
      };
    } catch (error) {
      console.log("Lỗi upload:", error);
      return null;
    }
  };

  const handleChangeImg = async ({ fileList }) => {
    const urlList = [];

    for (let file of fileList) {
      if (file.originFileObj) {
        const uploaded = await uploadToCloudinary(file.originFileObj);
        if (uploaded) {
          urlList.push(uploaded.url);
        }
      } else {
        const existingUrl = file.url || file;
        urlList.push(existingUrl);
      }
    }

    setImageList(urlList);
  }

  const handleAddVariant = () => {
    const newVariant = {
      id: null,
      tempKey: Date.now(),
      color: '',
      size: '',
      price: null,
      stock: null
    };
    setVariants([...variants, newVariant]);
  };

  const getVariantKey = (variant) => variant.id ?? variant.tempKey;
  // Hàm xóa một biến thể
  const handleRemoveVariant = (rowKey) => {
    setVariants(variants.filter(variant => getVariantKey(variant) !== rowKey));
  };

  const handleChangeVariant = (rowKey, field, value) => {
    const updatedVariants = variants.map(variant => {
      if (getVariantKey(variant) === rowKey) {
        return { ...variant, [field]: value };
      }
      return variant;
    });
    setVariants(updatedVariants);
  };


  // Thêm dòng thuộc tính mới
  const handleAddAttribute = () => {
    const newAttribute = {
      id: Date.now(),
      name: '',
      value: ''
    };
    setAttributes([...attributes, newAttribute]);
  };

  // Xóa một dòng thuộc tính
  const handleRemoveAttribute = (id) => {
    setAttributes(attributes.filter(attr => attr.id !== id));
  };

  // Cập nhật giá trị khi gõ
  const handleChangeAttribute = (id, field, value) => {
    const updatedAttributes = attributes.map(attr => {
      if (attr.id === id) {
        return { ...attr, [field]: value };
      }
      return attr;
    });
    setAttributes(updatedAttributes);
  };

  const handleSubmit = async () => {
    console.log(await form.validateFields());
    console.log("images", imageList)
    console.log("variants", variants);
    console.log("attributes", attributes);

    const values = await form.validateFields();

    try {
      if (variants.length === 0) {
        api.error({
          message: "Lỗi",
          description: "Phải có ít nhất 1 biến thể"
        });
        return;
      }

      for (let v of variants) {
        if (!v.color || !v.size || v.stock === null || v.stock === undefined) {
          api.error({
            message: "Lỗi biến thể",
            description: "Vui lòng nhập đầy đủ màu sắc, kích cỡ và tồn kho"
          });
          return;
        }
      }

      for (let attr of attributes) {
        if (!attr.name || !attr.value) {
          api.error({
            message: "Lỗi thuộc tính",
            description: "Tên và giá trị thuộc tính không được để trống"
          });
          return;
        }
      }

      const attributesJson = {};
      attributes.forEach(attr => {
        if (attr.name) {
          attributesJson[attr.name] = attr.value;
        }
      });

      const payload = {
        name: values.name,
        brandName: values.brandName,
        price: values.price,
        salePrice: values.sale_price,
        description: values.description,
        weight: values.weight,
        length: values.length,
        width: values.width,
        height: values.height,
        categoryId: values.category,
        images: imageList,
        variants: variants.map(v => ({
          id: v.id || null,
          color: v.color,
          size: v.size,
          stock: v.stock
        })),
        attributesJson: attributesJson,
        status: values.status
      };

      const res = isEdit
        ? await put(`shop/products/${id}`, payload)
        : await post("shop/products", payload)
        ;

      if (res.ok) {
        api.success({
          title: isEdit ? "Cập nhật thành công" : "Tạo sản phẩm thành công",
          description: isEdit
            ? "Sản phẩm đã được cập nhật và gửi duyệt lại."
            : "Sản phẩm đã được tạo và đang chờ duyệt."
        });

        navigate("/shop/products")
      }

    } catch (error) {
      console.log(error);
    }
  }

  return (
    <>
      {contextHolder}
      <div className="edit-product">
        <div className="title">
          {isEdit ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm mới"}
        </div>
        <div className="box">
          <div className="box__header">
            <MdOutlineImage className="box__header__icon" />
            <div className="box__header__title">
              Hình ảnh sản phẩm
            </div>
          </div>

          <div className="box__content">
            <Upload
              listType="picture-card"
              onChange={handleChangeImg}
              multiple
              beforeUpload={() => false} // chặn antd auto upload
              fileList={imageList.map((url, index) => ({
                uid: index.toString(),
                name: `image-${index}`,
                status: 'done',
                url: url,
              }))}
            >
              <div>
                <TiPlusOutline />
                <div style={{ marginTop: 8 }}>Thêm ảnh</div>
              </div>
            </Upload>
          </div>
        </div>

        <Form
          form={form}
          onFinish={handleSubmit}
          layout="vertical"
          name="basic_info_form"
        >
          <Form.Item
            name="status"
            hidden
          >
            <Input hidden />
          </Form.Item>
          <div className="box">
            <div className="box__header">
              <BsInfoSquare className="box__header__icon" />
              <div className="box__header__title">
                Thông tin cơ bản
              </div>
            </div>

            <div className="box__content">
              {/* <Form
              form={form}
              layout="vertical"
              name="basic_info_form"
            > */}
              <Row gutter={16}>
                <Col span={16}>
                  <Form.Item
                    name="name"
                    label="TÊN SẢN PHẨM"
                    rules={[{ required: true, message: 'Vui lòng nhập tên sản phẩm!' }]}
                  >
                    <Input placeholder="Ví dụ: Áo Sơ Mi Linen Cổ Tàu Premium" />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="brandName"
                    label="TÊN THƯƠNG HIỆU"
                  >
                    <Input placeholder="Ví dụ: ADIDAS" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={16}>
                <Col span={8}>
                  <Form.Item
                    name="price"
                    label="GIÁ SẢN PHẨM"
                    rules={[{ required: true, message: 'Vui lòng nhập giá sản phẩm!' }]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="VD: 150000"
                      formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                      parser={(value) => value?.replace(/\$\s?|(,*)/g, '') || ''}
                    />
                  </Form.Item>
                </Col>
                <Col span={8}>
                  <Form.Item
                    name="category"
                    label="DANH MỤC"
                    rules={[{ required: true, message: 'Vui lòng chọn danh mục!' }]}
                  >
                    <Select
                      placeholder="Chọn danh mục"
                      // onChange={handleChangeVariant}
                      options={categories.map(c => ({
                        value: c.id,
                        label: c.name
                      }))}
                    />
                  </Form.Item>
                </Col>

                <Col span={8}>
                  <Form.Item
                    name="sale_price"
                    label="GIÁ KHUYẾN MẠI"
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="VD: 150000"
                      formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                      parser={(value) => value?.replace(/\$\s?|(,*)/g, '') || ''}
                    />
                  </Form.Item>
                </Col>
              </Row>

              {/* Hàng 3: Mô tả sản phẩm */}
              <Form.Item
                name="description"
                label="MÔ TẢ SẢN PHẨM"
                rules={[{ required: true, message: 'Vui lòng viết mô tả sản phẩm!' }]}
              >
                <TextArea
                  rows={4}
                  placeholder="Nhập chi tiết về kiểu dáng, cảm giác khi mặc, cách bảo quản..."
                />
              </Form.Item>

            </div>
          </div>

          <div className="box">
            <div className="box__header">
              <MdLocalShipping className="box__header__icon" />
              <div className="box__header__title">
                Thông số vận chuyển
              </div>
            </div>

            <div className="box__content">
              <Row gutter={16}>
                <Col span={6}>
                  <Form.Item
                    name="weight"
                    label="CÂN NẶNG (gram)"
                    rules={[
                      { required: true, message: "Vui lòng nhập cân nặng!" }
                    ]}
                  >
                    <InputNumber
                      min={1}
                      style={{ width: "100%" }}
                      placeholder="VD: 300"
                    />
                  </Form.Item>
                </Col>

                <Col span={6}>
                  <Form.Item
                    name="length"
                    label="CHIỀU DÀI (cm)"
                    rules={[
                      { required: true, message: "Vui lòng nhập chiều dài!" }
                    ]}
                  >
                    <InputNumber
                      min={1}
                      style={{ width: "100%" }}
                      placeholder="VD: 20"
                    />
                  </Form.Item>
                </Col>

                <Col span={6}>
                  <Form.Item
                    name="width"
                    label="CHIỀU RỘNG (cm)"
                    rules={[
                      { required: true, message: "Vui lòng nhập chiều rộng!" }
                    ]}
                  >
                    <InputNumber
                      min={1}
                      style={{ width: "100%" }}
                      placeholder="VD: 20"
                    />
                  </Form.Item>
                </Col>

                <Col span={6}>
                  <Form.Item
                    name="height"
                    label="CHIỀU CAO (cm)"
                    rules={[
                      { required: true, message: "Vui lòng nhập chiều cao!" }
                    ]}
                  >
                    <InputNumber
                      min={1}
                      style={{ width: "100%" }}
                      placeholder="VD: 10"
                    />
                  </Form.Item>
                </Col>
              </Row>
            </div>
          </div>

          <div className="box">
            <div className="box__header variant__header">
              <div className="box__header__left">
                <BsLayers className="box__header__icon" />
                <div className="box__header__title">Phân loại & Biến thể</div>
              </div>

              <Button
                type="primary"
                ghost
                icon={<FiPlusCircle />}
                className="btn-add-variant"
                onClick={handleAddVariant}
              >
                Thêm biến thể
              </Button>
            </div>

            <div className="box__content">
              <div className="variant-table">
                <div className="variant-table__head">
                  <div className="col-color">MÀU SẮC</div>
                  <div className="col-size">KÍCH CỠ</div>
                  {/* <div className="col-price">GIÁ BÁN (đ)</div> */}
                  <div className="col-stock">TỒN KHO</div>
                  <div className="col-action"></div>
                </div>

                <div className="variant-table__body">
                  {variants.map((variant) => {
                    const rowKey = variant.id ?? variant.tempKey;

                    return (
                      <div className="variant-table__row" key={rowKey}>

                        <div className="col-color">
                          <Input
                            placeholder="VD: Trắng"
                            value={variant.color}
                            onChange={(e) => handleChangeVariant(rowKey, 'color', e.target.value)}
                            className="variant-input"
                          />
                        </div>

                        <div className="col-size">
                          <Input
                            placeholder="VD: M"
                            value={variant.size}
                            onChange={(e) => handleChangeVariant(rowKey, 'size', e.target.value)}
                            className="variant-input size-input"
                          />
                        </div>

                        {/* Ô nhập Giá bán */}
                        {/* <div className="col-price">
                      <InputNumber
                        placeholder="0"
                        value={variant.price}
                        onChange={(value) => handleChangeVariant(variant.id, 'price', value)}
                        formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                        parser={(value) => value?.replace(/\$\s?|(,*)/g, '') || ''}
                        bordered={false}
                        className="variant-input"
                        style={{ width: '100%' }}
                      />
                    </div> */}

                        <div className="col-stock">
                          <InputNumber
                            placeholder="0"
                            value={variant.stock}
                            onChange={(value) => handleChangeVariant(rowKey, 'stock', value)}
                            className="variant-input"
                            style={{ width: '100%' }}
                          />
                        </div>

                        {/* Nút Xóa */}
                        <div className="col-action">
                          <button
                            type="button"
                            className="btn-delete"
                            onClick={() => handleRemoveVariant(rowKey)}
                            title="Xóa biến thể này"
                          >
                            <FiTrash2 />
                          </button>
                        </div>
                      </div>
                    )
                  }
                  )}

                  {/* Hiển thị thông báo nếu mảng rỗng */}
                  {variants.length === 0 && (
                    <div className="empty-state">
                      Chưa có biến thể nào. Hãy bấm "Thêm biến thể".
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="box">
            {/* HEADER */}
            <div className="box__header variant__header"> {/* Tận dụng lại class header của variant */}
              <div className="box__header__left">
                <BsTags className="box__header__icon" />
                <div className="box__header__title">Thuộc tính sản phẩm</div>
              </div>

              <Button
                type="primary"
                ghost
                icon={<FiPlusCircle />}
                className="btn-add-variant" // Tận dụng lại class nút thêm
                onClick={handleAddAttribute}
              >
                Thêm thuộc tính
              </Button>
            </div>

            {/* NỘI DUNG BẢNG THUỘC TÍNH */}
            <div className="box__content">
              <div className="attribute-table">
                {/* Tiêu đề cột */}
                <div className="attribute-table__head">
                  <div className="col-attr-name">TÊN THUỘC TÍNH</div>
                  <div className="col-attr-value">GIÁ TRỊ</div>
                  <div className="col-action"></div>
                </div>

                {/* Danh sách các dòng nhập liệu */}
                <div className="attribute-table__body">
                  {attributes.map((attr) => (
                    <div className="attribute-table__row" key={attr.id}>

                      {/* Ô Tên thuộc tính */}
                      <div className="col-attr-name">
                        <Input
                          placeholder="VD: Chất liệu, Xuất xứ..."
                          value={attr.name}
                          onChange={(e) => handleChangeAttribute(attr.id, 'name', e.target.value)}
                          className="variant-input" // Dùng lại style input của variant
                        />
                      </div>

                      {/* Ô Giá trị */}
                      <div className="col-attr-value">
                        <Input
                          placeholder="VD: Cotton, Việt Nam..."
                          value={attr.value}
                          onChange={(e) => handleChangeAttribute(attr.id, 'value', e.target.value)}
                          className="variant-input"
                        />
                      </div>

                      {/* Nút Xóa */}
                      <div className="col-action">
                        <button
                          className="btn-delete" // Dùng lại style nút xóa
                          onClick={() => handleRemoveAttribute(attr.id)}
                          title="Xóa thuộc tính này"
                        >
                          <FiTrash2 />
                        </button>
                      </div>
                    </div>
                  ))}

                  {/* Trạng thái trống */}
                  {attributes.length === 0 && (
                    <div className="empty-state">
                      Chưa có thuộc tính nào. Bấm "Thêm thuộc tính" để bổ sung chi tiết.
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>

          <div className="action-bar">
            {/* <Button
              type="text"
              className="btn-draft"
              // onClick={() => handleSubmit('DRAFT')}
              htmlType="submit"
            >
              Lưu bản nháp
            </Button> */}

            <Button
              type="primary"
              className="btn-publish"
              htmlType="submit"
            >
              {isEdit ? "Lưu" : "Gửi duyệt"}
            </Button>
          </div>

        </Form>
      </div >
    </>
  )
}

export default EditProduct;