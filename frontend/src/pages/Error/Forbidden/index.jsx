import { Result, Button } from "antd";
import { useNavigate } from "react-router-dom";

function Forbidden() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: "40px 16px" }}>
      <Result
        status="403"
        title="403"
        subTitle="Bạn không có quyền truy cập trang này."
        extra={[
          <Button type="primary" key="home" onClick={() => navigate("/")}>
            Về trang chủ
          </Button>,
          <Button key="back" onClick={() => navigate(-1)}>
            Quay lại
          </Button>,
        ]}
      />
    </div>
  );
}

export default Forbidden;