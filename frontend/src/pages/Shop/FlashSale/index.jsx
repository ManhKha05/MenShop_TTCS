import { Input, Select, Spin } from "antd";
import "./FlashSale.scss";
import { IoSearch } from "react-icons/io5";
import FlashSaleTable from "../../../components/FlashSaleTable";
import { useEffect, useState } from "react";
import ModalRegister from "./ModalRegister";
import { get } from "../../../utils/request";
import { connectSocket, subscribeSocket, unsubscribe } from "../../../utils/socket";


function FlashSale() {
  const [openRegister, setOpenRegister] = useState(false);
  const [flashSaleId, setFlashSaleId] = useState();
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(5);
  const [total, setTotal] = useState();
  const [status, setStatus] = useState("");
  const [keyword, setKeyword] = useState("");
  const [flashSales, setFlashSales] = useState([]);
  const [reload, setReload] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    const fetchApi = async () => {
      try {
        const [flashSalesRes] = await Promise.all([
          get('shop/flash-sale', {
            page: page - 1,
            size: pageSize,
            keyword,
            status
          })
        ])

        const flashSalesData = await flashSalesRes.json();

        setFlashSales(flashSalesData.content);
        setTotal(flashSalesData.totalElements);
      } catch (error) {
        console.error(error);
      } finally {
        setLoading(false);
      }
    }
    fetchApi();
  }, [keyword, status, page, pageSize, reload])

  useEffect(() => {
    let subscription;

    connectSocket(() => {
      subscription = subscribeSocket(`/topic/flash-sale`, (data) => {
        if (!data) return;

        setReload(!reload);
      });
    });

    return () => {
      subscription?.unsubscribe();
    };
  }, []);

  // if (loading) {
  //   return (
  //     <div className="dashboard-admin dashboard-admin--loading">
  //       <Spin size="large" />
  //     </div>
  //   );
  // }

  return (
    <>
      <div className="flashsale-shop">
        <div className="title">
          Tham gia các chương trình Flash Sale để thu hút khách hàng
        </div>

        <div className="filter">
          <Input placeholder="Tìm tên chương trình" prefix={<IoSearch />} />
          <Select
            defaultValue=""
            style={{ width: 190 }}
            onChange={(e) => setStatus(e)}
            options={[
              { value: '', label: 'Tất cả trạng thái' },
              { value: 'UPCOMING', label: 'Sắp diễn ra' },
              { value: 'ACTIVE', label: 'Đang diễn ra' },
              { value: 'ENDED', label: 'Đã kết thúc' }
            ]}
          />
        </div>

        <FlashSaleTable
          role="SHOP"
          data={flashSales}
          page={page}
          pageSize={pageSize}
          total={total}
          onRegister={(id) => {
            setOpenRegister(true);
            setFlashSaleId(id);
          }}
          onPageChange={(p) => setPage(p)}
          onPageSizeChange={(s) => {
            setPageSize(s);
            setPage(1);
          }}
        />
      </div>

      <ModalRegister open={openRegister} flashSaleId={flashSaleId} onCancel={() => setOpenRegister(false)} />
    </>
  )
}

export default FlashSale;