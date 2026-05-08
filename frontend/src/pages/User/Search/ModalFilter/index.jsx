import { Modal, Button } from "antd"
import { useState } from "react";
import { LuFilter } from "react-icons/lu";
import { IoStarSharp } from "react-icons/io5";
import "./ModalFilter.scss"

function ModalFilter({ onApplyFilter }) {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [selectedStar, setSelectedStar] = useState(null);

  const [activePriceId, setActivePriceId] = useState(null);
  const [priceRange, setPriceRange] = useState({ min: null, max: null });

  const handleOk = () => {
    if (onApplyFilter) {
      onApplyFilter({
        minStar: selectedStar,
        minPrice: priceRange.min,
        maxPrice: priceRange.max
      });
    }
    setIsModalOpen(false);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
  };

  const handlePriceClick = (id, min, max) => {
    if (activePriceId === id) {
      setActivePriceId(null);
      setPriceRange({ min: null, max: null });
    } else {
      setActivePriceId(id);
      setPriceRange({ min, max });
    }
  };

  const handleStarClick = (starValue) => {
    if (selectedStar === starValue) {
      setSelectedStar(null);
    } else {
      setSelectedStar(starValue);
    }
  };

  const handleClearFilter = () => {
    setSelectedStar(null);
    setActivePriceId(null);
    setPriceRange({ min: null, max: null });
    if (onApplyFilter) {
      onApplyFilter({ minStar: null, minPrice: null, maxPrice: null });
    }
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
        style={{ marginTop: '-50px' }}
        footer={[
          <Button key="clear" onClick={handleClearFilter}>
            Xóa bộ lọc
          </Button>,
          <Button key="cancel" onClick={handleCancel}>
            Hủy
          </Button>,
          <Button key="submit" type="primary" onClick={handleOk}>
            Xem kết quả
          </Button>,
        ]}
      >
        <div className="modalfilter">

          <div className="modalfilter__field">
            <div className="modalfilter__title">Đánh giá</div>

            <div className="modalfilter__checkbox">
              <input
                type="radio" id="5star" name="rate"
                checked={selectedStar === 5}
                onClick={() => handleStarClick(5)}
                readOnly
              />
              <label htmlFor="5star">
                <IoStarSharp /><IoStarSharp /><IoStarSharp /><IoStarSharp /><IoStarSharp />
                <span>từ 5 sao</span>
              </label>
            </div>

            <div className="modalfilter__checkbox">
              <input
                type="radio" id="4star" name="rate"
                checked={selectedStar === 4}
                onClick={() => handleStarClick(4)}
                readOnly
              />
              <label htmlFor="4star">
                <IoStarSharp /><IoStarSharp /><IoStarSharp /><IoStarSharp /><IoStarSharp className="none" />
                <span>từ 4 sao</span>
              </label>
            </div>

            <div className="modalfilter__checkbox">
              <input
                type="radio" id="3star" name="rate"
                checked={selectedStar === 3}
                onClick={() => handleStarClick(3)}
                readOnly
              />
              <label htmlFor="3star">
                <IoStarSharp /><IoStarSharp /><IoStarSharp /><IoStarSharp className="none" /><IoStarSharp className="none" />
                <span>từ 3 sao</span>
              </label>
            </div>
          </div>

          <div className="modalfilter__field">
            <div className="modalfilter__title">Giá</div>
            <button
              className={`modalfilter__price ${activePriceId === 1 ? 'active' : ''}`}
              onClick={() => handlePriceClick(1, 0, 250000)}
            >
              Dưới 250.000
            </button>
            <button
              className={`modalfilter__price ${activePriceId === 2 ? 'active' : ''}`}
              onClick={() => handlePriceClick(2, 250000, 450000)}
            >
              250.000 - 450.000
            </button>
            <button
              className={`modalfilter__price ${activePriceId === 3 ? 'active' : ''}`}
              onClick={() => handlePriceClick(3, 450000, 1050000)}
            >
              450.000 - 1.050.000
            </button>
            <button
              className={`modalfilter__price ${activePriceId === 4 ? 'active' : ''}`}
              onClick={() => handlePriceClick(4, 1050000, null)}
            >
              Trên 1.050.000
            </button>
          </div>

        </div>
      </Modal>
    </>
  )
}

export default ModalFilter;

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