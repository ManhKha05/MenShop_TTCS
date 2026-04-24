import { Modal } from "antd"
import { useState } from "react";
import { LuFilter } from "react-icons/lu";
import { IoStarSharp } from "react-icons/io5";
import "./ModalFilter.scss"

function ModalFilter() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const showModal = () => {
    setIsModalOpen(true);
  };
  const handleOk = () => {
    setIsModalOpen(false);
  };
  const handleCancel = () => {
    setIsModalOpen(false);
  };

  return (
    <>
      <button
        className="search__filter__all"
        onClick={() => setIsModalOpen(true)}
      >
        <LuFilter />
        Tất cả
      </button>

      <Modal
        title="Tất cả bộ lọc"
        closable={{ 'aria-label': 'Custom Close Button' }}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={handleCancel}
        okText="Xem kết quả"
        cancelText="Hủy"
        style={{marginTop: '-50px'}}
      >
        <div className="modalfilter">
          <div className="modalfilter__field">
            <div className="modalfilter__title">
              Đánh giá
            </div>
            <div className="modalfilter__checkbox">
              <input type="radio" id="5star" value="5-star" name="rate" />
              <label htmlFor="5star">
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp />
                <span>từ 5 sao</span>
              </label>
            </div>
            <div className="modalfilter__checkbox">
              <input type="radio" id="4star" value="4-star" name="rate" />
              <label htmlFor="4star">
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp className="none" />
                <span>từ 4 sao</span>
              </label>
            </div>
            <div className="modalfilter__checkbox">
              <input type="radio" id="3star" value="3-star" name="rate" />
              <label htmlFor="3star">
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp />
                <IoStarSharp className="none" />
                <IoStarSharp className="none" />
                <span>từ 3 sao</span>
              </label>
            </div>
          </div>

          <div className="modalfilter__field">
            <div className="modalfilter__title">
              Giá
            </div>
            <button className="modalfilter__price">
              Dưới 250.000
            </button>
            <button className="modalfilter__price">
              250.000 - 450.000
            </button>
            <button className="modalfilter__price">
              450.000 - 1.050.000
            </button>
            <button className="modalfilter__price">
              Trên 1.050.000
            </button>
          </div>

          {/* <div className="modalfilter__field">
            <div className="modalfilter__title">
              Nhà cung cấp
            </div>
            <div className="modalfilter__checkbox">
              <input type="checkbox" id="ncc-1" value="5-star" name="rate" />
              <label htmlFor="ncc-1">
                Điện tử HI-END
              </label>
            </div>

            <div className="modalfilter__checkbox">
              <input type="checkbox" id="ncc-2" value="5-star" name="rate" />
              <label htmlFor="ncc-2">
                TEEMO PC OFFICIAL STORE
              </label>
            </div>
            
          </div> */}
        </div>
      </Modal>
    </>
  )
}

export default ModalFilter;