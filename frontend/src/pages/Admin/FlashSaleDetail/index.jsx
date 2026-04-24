import "./FlashSaleDetail.scss";
import { IoMdArrowRoundBack } from "react-icons/io";
import { Input, Select } from "antd";
import { Link, useParams } from "react-router-dom";
import TableProduct from "./TableProduct";


function FlashSaleDetail() {
  const {id} = useParams();

  
  return (
    <>
      <div className="flashsale-detail-admin">
        <Link to="/admin/flash-sale" className="back">
          <IoMdArrowRoundBack />
          Quay lại
        </Link>
        <div className="title">
          Sản phẩm Flash Sale <span>"Flash Sale Giờ Vàng"</span>
        </div>

        {/* <div className="row">
          <div className="filter">
            <div className="item">
              <div className="title">
                Tìm kiếm theo sản phẩm
              </div>
              <Input placeholder="Nhập tên sản phẩm" />
            </div>

            <div className="item">
              <div className="title">
                Trạng thái
              </div>
              <Select
                defaultValue="ALL"
                style={{ width: 150 }}
                // onChange={handleChange}
                options={[
                  { value: 'ALL', label: 'Tất cả' },
                  { value: 'PENDING', label: 'Chờ duyệt' },
                  { value: 'APPROVED', label: 'Đã duyệt' },
                  { value: 'REJECTED', label: 'Từ chối' }
                ]}
              />
            </div>
          </div>

          <div className="stat">
            <div className="title">
              Yêu cầu đang chờ
            </div>
            <div className="value">
              124
            </div>
          </div>
        </div> */}

        <div className="table">
          <TableProduct flashSaleId={id} />
        </div>
      </div>
    </>
  )
}

export default FlashSaleDetail;