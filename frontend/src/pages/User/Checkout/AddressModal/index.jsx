import "./AddressModal.scss";
import { Modal } from "antd";
import { useEffect, useState } from "react";
import { IoMdAddCircle } from "react-icons/io";
import AddressEdit from "../../../../components/AddressEdit";


function AddressModal({ open, onCancel, selectedAddress, otherAddresses, onSelectAddress, onReloadAddresses}) {
  const [openEdit, setOpenEdit] = useState(false);
  const [editAddress, setEditAddress] = useState({});
  const [currentSelected, setCurrentSelected] = useState(null);

  useEffect(() => {
    setCurrentSelected(selectedAddress || null);
  }, [selectedAddress, open]);

  const handleOk = () => {
    if (currentSelected) {
      onSelectAddress(currentSelected);
    }
  };

  return (
    <>
      <Modal
        open={open}
        onCancel={onCancel}
        footer={null}
        width={"600px"}
      >
        <div className="address-modal">
          <div className="title">
            Địa chỉ của tôi
          </div>

          <div className="list">
            {selectedAddress && (
              <button
                type="button"
                className={`item ${currentSelected?.id === selectedAddress.id ? "selected" : ""}`}
                onClick={() => setCurrentSelected(selectedAddress)}
              >
                <div className="item__row">
                  <input
                    type="checkbox"
                    checked={currentSelected?.id === selectedAddress.id}
                    readOnly
                    className="item__checkbox"
                  />
                  <div className="item__info">
                    <div className="item__top">
                      <div className="item__name">
                        {selectedAddress.receiverName}
                      </div>
                      <div className="item__phone">
                        {selectedAddress.phone}
                      </div>
                    </div>

                    <div className="item__address">
                      {selectedAddress.address}
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  className="item__action"
                  onClick={(e) => {
                    e.stopPropagation();
                    setOpenEdit(true);
                    setEditAddress(selectedAddress);
                  }}
                >
                  Cập nhật
                </button>
              </button>
            )}

            {otherAddresses.map((item) => (
              <button
                type="button"
                key={item.id}
                className={`item ${currentSelected?.id === item.id ? "selected" : ""}`}
                onClick={() => setCurrentSelected(item)}
              >
                <div className="item__row">
                  <input
                    type="checkbox"
                    checked={currentSelected?.id === item.id}
                    readOnly
                    className="item__checkbox"
                  />
                  <div className="item__info">
                    <div className="item__top">
                      <div className="item__name">
                        {item.receiverName}
                      </div>
                      <div className="item__phone">
                        {item.phone}
                      </div>
                    </div>

                    <div className="item__address">
                      {item.address}
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  className="item__action"
                  onClick={(e) => {
                    e.stopPropagation();
                    setOpenEdit(true);
                    setEditAddress(item);
                  }}
                >
                  Cập nhật
                </button>
              </button>
            ))}
          </div>

          <button
            className="add"
            onClick={() => {
              setOpenEdit(true);
              setEditAddress(null);
            }}
          >
            <IoMdAddCircle />
            Thêm địa chỉ mới
          </button>

          <div className="action">
            <button className="btn btn--default" onClick={onCancel}>
              Hủy
            </button>

            <button className="btn btn--primary" onClick={handleOk}>
              Xác nhận
            </button>
          </div>
        </div>
      </Modal>

      <AddressEdit
        open={openEdit}
        onCancel={() => {
          setOpenEdit(false);
        }}
        record={editAddress}
        onReload={null}
        onSuccess={() => {
          setOpenEdit(false);
          onReloadAddresses();
        }}
      />
    </>
  )
}

export default AddressModal;