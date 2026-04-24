import { Result, Button } from "antd";
import { useNavigate } from "react-router-dom";

function ServerError() {
  const navigate = useNavigate();

  return (
    <div style={{ padding: "40px 16px" }}>
      <Result
        status="500"
        title="500"
        subTitle="Đã có lỗi xảy ra từ hệ thống. Vui lòng thử lại sau."
        extra={[
          <Button type="primary" key="home" onClick={() => navigate("/")}>
            Về trang chủ
          </Button>,
          <Button key="reload" onClick={() => window.location.reload()}>
            Thử lại
          </Button>,
        ]}
      />
    </div>
  );
}

export default ServerError;