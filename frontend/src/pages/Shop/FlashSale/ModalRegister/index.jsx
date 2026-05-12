import "./ModalRegister.scss";
import { Button, Checkbox, Input, InputNumber, Modal, Table, notification } from "antd";
import { useEffect, useState } from "react";
import { BiSearchAlt } from "react-icons/bi";
import { get, post } from "../../../../utils/request";


function ModalRegister({ open, flashSaleId, onCancel }) {
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState(12);
  const [keyword, setKeyword] = useState("");
  const [products, setProducts] = useState([]);
  const [selectedMap, setSelectedMap] = useState({});

  useEffect(() => {
    if (open) fetchProducts();
  }, [open, keyword, page, pageSize]);

  const fetchProducts = async () => {
    const res = await get(`shop/flash-sale/${flashSaleId}/products`, {
      page: page - 1,
      size: pageSize,
      keyword,
    });

    const data = await res.json();

    console.log(data.content);

    setProducts(
      data.content.map(p => ({
        id: p.id,
        name: p.name,
        image: p.image,
        price: p.price,
        stock: p.stock,

        selected: selectedMap[p.id]?.selected ?? p.isRegistered,
        flashPrice: selectedMap[p.id]?.flashPrice ?? p.flashPrice,
      }))
    );

    setTotal(data.totalElements);
  };


  const handleSelect = (id, checked) => {
    setProducts(prev =>
      prev.map(item =>
        item.id === id
          ? {
            ...item,
            selected: checked,
            flashPrice: checked ? item.flashPrice : null
          }
          : item
      )
    );

    setSelectedMap(prev => ({
      ...prev,
      [id]: {
        ...prev[id],
        selected: checked
      }
    }));
  };

  const handleChange = (id, field, value) => {
    setProducts(prev =>
      prev.map(item =>
        item.id === id
          ? { ...item, [field]: value }
          : item
      )
    );

    setSelectedMap(prev => ({
      ...prev,
      [id]: {
        ...prev[id],
        [field]: value
      }
    }));
  };

  const handleSearch = (e) => {
    if (e.key === "Enter") {
      setKeyword(e.target.value);
    }
  }

  const handleSubmit = async () => {
    if (!products || products.length === 0) {
      notification.error({
        message: "Lỗi",
        description: "Không có sản phẩm"
      });
      return;
    }

    const selected = products.filter(item => item.selected);

    if (selected.length === 0) {
      notification.warning({
        message: "Chưa chọn sản phẩm",
        description: "Vui lòng chọn ít nhất 1 sản phẩm"
      });
      return;
    }

    for (let item of selected) {
      if (!item.flashPrice) {
        notification.error({
          message: "Thiếu dữ liệu",
          description: `Nhập giá flash cho: ${item.name}`
        });
        return;
      }

      if (item.flashPrice >= item.price) {
        notification.error({
          message: "Giá không hợp lệ",
          description: `Giá flash phải nhỏ hơn giá gốc: ${item.name}`
        });
        return;
      }
    }

    const payload = {
      flashSaleId,
      items: Object.entries(selectedMap).map(([id, value]) => ({
        productId: Number(id),
        flashPrice: value.selected ? value.flashPrice : null,
        selected: value.selected
      }))
    };

    try {
      const res = await post("shop/flash-sale/register", payload);

      if (!res.ok) throw new Error();

      notification.success({
        message: "Thành công",
        description: "Đăng ký thành công"
      });

      onCancel();

    } catch {
      notification.error({
        message: "Thất bại",
        description: "Đăng ký không thành công"
      });
    }
  };


  const columns = [
    {
      title: '',
      dataIndex: 'selected',
      render: (_, record) => (
        <Checkbox
          checked={record.selected}
          onChange={(e) => handleSelect(record.id, e.target.checked)}
        />
      ),
      width: 50,
    },
    {
      title: 'Sản phẩm',
      key: 'info',
      render: ({ image, name }) => (
        <div className="info">
          <img src={image} alt="" className="image" />
          <div className="name">
            {name}
          </div>
        </div>
      )
    },
    {
      title: 'Giá gốc',
      key: 'price',
      render: ({ price }) => (
        <div className="price">
          {price}đ
        </div>
      )
    },
    {
      title: 'Tồn kho',
      dataIndex: 'stock',
      key: 'stock',
    },
    {
      title: 'Giá Flash Sale',
      key: 'flashPrice',
      render: (record) => (
        record.selected ? (
          <InputNumber
            min={1}
            max={record.price - 1}
            value={record.flashPrice}
            onChange={(value) => handleChange(record.id, 'flashPrice', value)}
          />
        ) : (
          '-'
        )
      )
    },
    {
      title: '% Giảm',
      render: (record) => {
        if (!record.flashPrice) return '-';

        const percent = Math.round(
          (1 - record.flashPrice / record.price) * 100
        );

        return <span style={{ color: 'red' }}>{percent}%</span>;
      }
    }
    // {
    //   title: 'Số lượng',
    //   dataIndex: 'flashStock',
    //   render: (_, record) =>
    //     record.selected ? (
    //       <InputNumber
    //         min={1}
    //         max={record.stock}
    //         value={record.flashStock}
    //         onChange={(value) => handleChange(record.id, 'flashStock', value)}
    //       />
    //     ) : (
    //       '-'
    //     ),
    // },
  ];

  return (
    <>
      <Modal
        open={open}
        onCancel={onCancel}
        width="1000px"
        footer={null}
      >
        <div className="flashsale-shop">
          <div className="modal">
            <div className="title">
              Đăng ký sản phẩm tham gia Flash Sale
            </div>

            <div className="search">
              <Input placeholder="Tìm kiếm sản phẩm" prefix={<BiSearchAlt />} onKeyDown={handleSearch} />
            </div>

            <Table
              rowKey="id"
              dataSource={products}
              columns={columns}
              pagination={{
                current: page,
                pageSize,
                total,
                showSizeChanger: true,
                pageSizeOptions: [5, 10, 20],
                showTotal: (total) => `Tổng ${total} sản phẩm`
              }}
              onChange={pagination => {
                setPage(pagination.current)
                setPageSize(pagination.pageSize)
              }}
            />

            <div className="actions">
              <Button onClick={onCancel}>Hủy</Button>
              <Button
                type="primary"
                onClick={handleSubmit}
                disabled={!products?.some(p => p.selected)}
              >
                Đăng ký
              </Button>
            </div>
          </div>
        </div>
      </Modal>
    </>
  )
}

export default ModalRegister;