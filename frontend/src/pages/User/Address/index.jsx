import "./Address.scss";
import { MdAddLocationAlt } from "react-icons/md";
import { FaPhone } from "react-icons/fa6";
import { IoLocation } from "react-icons/io5";
import { FaCircleCheck } from "react-icons/fa6";
import { useEffect, useState } from "react";
import AddressEdit from "../../../components/AddressEdit"
import { del, get, patch } from "../../../utils/request";
import { notification } from "antd";

function Address() {
  const [openEdit, setOpenEdit] = useState(false);
  const [notificationApi, contextHolder] = notification.useNotification();
  const [reload, setReload] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState({});
  const [data, setData] = useState([]);

  useEffect(() => {
    const fetchApi = async () => {
      try {
        const res = await get("addresses");

        const dt = await res.json();

        setData(dt);
      } catch (error) {
        console.error(error);
      }
    }
    fetchApi();
  }, [reload])

  console.log(data)

  const handleDelete = (id) => {
    const fetchApi = async () => {
      try {
        const res = await del(`addresses/${id}`);

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }
        notificationApi.success({
          title: "Xóa địa chỉ thành công"
        })

        setReload(!reload);
      } catch (error) {
        console.error(error);
      }
    }
    fetchApi();
  }

  const handleDefault = (id) => {
    const fetchApi = async () => {
      try {
        const res = await patch(`addresses/${id}/default`);

        if (!res.ok) {
          const err = await res.json();
          throw new Error(err.message);
        }
        notificationApi.success({
          title: "Đặt địa chỉ mặc định thành công"
        })

        setReload(!reload);
      } catch (error) {
        console.error(error);
      }
    }
    fetchApi();
  }

  return (
    <>
      {contextHolder}
      <div className="address">
        <div className="header">
          <div className="left">
            <div className="title">
              Địa chỉ của tôi
            </div>
            <div className="subtitle">
              Quản lí các địa chỉ giao hàng để có trải nghiệm mua sắm nhanh chóng và thuận tiện hơn
            </div>
          </div>
          <button
            className="btn"
            onClick={() => {
              setOpenEdit(true)
              setSelectedAddress(null);
            }}
          >
            <MdAddLocationAlt />
            Thêm địa chỉ mới
          </button>
        </div>

        <div className="body">
          {data.map(item => (
            <div className={"card" + (item.default ? " default" : "")} key={item.id}>
              <div className="card__name">
                {item.receiverName}

                {item.default && (<span className="tag"><FaCircleCheck />Mặc định</span>)}
              </div>
              <div className="card__phone">
                <FaPhone />
                {item.phone}
              </div>
              <div className="card__address">
                <IoLocation />
                {item.address}
              </div>

              <div className="actions">
                <button
                  className="update"
                  onClick={() => {
                    setOpenEdit(true);
                    setSelectedAddress(item);
                  }}
                >
                  Chỉnh sửa
                </button>
                <button
                  className="del"
                  onClick={() => handleDelete(item.id)}
                >
                  Xóa
                </button>
                {!item.default && (
                  <button 
                  className="set-default"
                  onClick={() => handleDefault(item.id)}
                  >
                    Đặt làm địa chỉ mặc định
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>

      <AddressEdit
        open={openEdit}
        onCancel={() => {
          setOpenEdit(false)
        }}
        record={selectedAddress}
        onReload={() => setReload(!reload)}
      />
    </>
  )
}

export default Address;