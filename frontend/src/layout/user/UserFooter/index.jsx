import "./UserFooter.scss";
import { FaFacebook } from "react-icons/fa";
import { FaInstagram, FaTiktok  } from "react-icons/fa";

function UserFooter () {
  return (
    <>
      <div className="home-footer">
        <div className="container">
          <div className="home-footer__top">
            <div className="home-footer__brand">
              <img src="https://res.cloudinary.com/dcjraarbb/image/upload/v1772208191/Ch%E1%BB%A3_S%E1%BB%91_fdztr0.png" alt="" className="home-footer__brand__logo" />
              <div className="home-footer__brand__desc">
                Chợ Số — Sàn thương mại điện tử đa ngành hàng, kết nối người mua và người bán trên toàn quốc với hàng triệu sản phẩm chính hãng, giá tốt và dịch vụ tin cậy.
              </div>
            </div>
            <div className="home-footer__column">
              <h3 className="home-footer__title">
                Chăm sóc khách hàng
              </h3>
              <ul className="home-footer__links">
                <li className="home-footer__link">
                  Trung tâm trợ giúp
                </li>
                <li className="home-footer__link">
                  Chợ Số Blog
                </li>
                <li className="home-footer__link">
                  Hướng dẫn mua hàng
                </li>
              </ul>
            </div>
            <div className="home-footer__column">
              <h3 className="home-footer__title">
                Về Chợ Số
              </h3>
              <ul className="home-footer__links">
                <li className="home-footer__link">
                  Giới thiệu về Chợ Số
                </li>
                <li className="home-footer__link">
                  Chính sách bảo mật
                </li>
              </ul>
            </div>
            <div className="home-footer__column">
              <h3 className="home-footer__title">
                Theo dõi Chợ Số
              </h3>
              <ul className="home-footer__links">
                <li className="home-footer__link">
                  <FaFacebook /> Facebook
                </li>
                <li className="home-footer__link">
                  <FaInstagram /> Instagram
                </li>
                <li className="home-footer__link">
                  <FaTiktok /> Tiktok
                </li>
              </ul>
            </div>
          </div>
          <div className="home-footer__bottom">
            © 2026 Chợ Số. All rights reversed. Công ty Cổ phần Chợ Số
          </div>
        </div>
      </div>
    </>
  )
}

export default UserFooter;