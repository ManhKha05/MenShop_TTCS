import { Result, Button } from "antd";
import { useNavigate } from "react-router-dom";

function NotFound({ message = "Trang hoặc dữ liệu bạn tìm không tồn tại." }) {
  const navigate = useNavigate();

  return (
    <div style={{ padding: "40px 16px" }}>
      <Result
        status="404"
        title="404"
        subTitle={message}
        extra={[
          <Button type="primary" key="home" onClick={() => navigate("/")}>
            Về trang chủ
          </Button>,
          // <Button key="back" onClick={() => navigate(-1)}>
          //   Quay lại
          // </Button>,
        ]}
      />
    </div>
  );
}

export default NotFound;