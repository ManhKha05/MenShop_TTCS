import "./Home.scss";
import { AiOutlineThunderbolt } from "react-icons/ai";
import { Carousel } from "antd";
import { useEffect, useState } from "react";
import { get } from "../../../utils/request";
import { Link } from "react-router-dom"
import { connectSocket, subscribeSocket, unsubscribe } from "../../../utils/socket";

function Home() {
  const [banners, setBanners] = useState([]);
  const [categories, setCategories] = useState([]);
  const [flashSale, setFlashSale] = useState(null);
  const [timeLeft, setTimeLeft] = useState(null);
  const [reload, setReload] = useState(false);

  useEffect(() => {
    const fetchApi = async () => {
      try {
        const [bannersRes, categoriesRes, flashSaleRes] = await Promise.all([
          get("banners"),
          get("categories/parents"),
          get("flash-sale/active")
        ]);

        const bannersData = await bannersRes.json();
        const categoriesData = await categoriesRes.json();
        const flashSaleData = await flashSaleRes.json();

        setCategories(categoriesData);
        setFlashSale(flashSaleData);

        // Group Banners
        let groupBanners = [];
        for (let i = 0; i < bannersData.length; i += 2) {
          groupBanners.push(bannersData.slice(i, i + 2));
        }
        setBanners(groupBanners);

      } catch (error) {
        console.log("Lỗi", error);
      }
    }
    fetchApi();
  }, [reload])

  // console.log(flashSale);

  useEffect(() => {
    if (!flashSale?.endTime) return;

    const interval = setInterval(() => {
      const now = new Date().getTime();
      const end = new Date(flashSale.endTime).getTime();

      const diff = end - now;

      if (diff <= 0) {
        setTimeLeft(null);
        clearInterval(interval);
        return;
      }

      const hours = Math.floor(diff / (1000 * 60 * 60));
      const minutes = Math.floor((diff / (1000 * 60)) % 60);
      const seconds = Math.floor((diff / 1000) % 60);

      setTimeLeft({
        hours,
        minutes,
        seconds
      });

    }, 1000);

    return () => clearInterval(interval);
  }, [flashSale]);

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

  const format = (num) => String(num).padStart(2, "0");

  return (
    <>
      <div className="home">
        <div className="container">
          <Carousel autoplay autoplaySpeed={4000}>
            {banners.map((item, index) => (
              <div key={index}>
                <div className="home__banner">
                  <div href="/" className="home__banner-one">
                    <img src={item[0]?.imageUrl} alt="" />
                  </div>
                  <div href="/" className="home__banner-two">
                    <img src={item[1]?.imageUrl} alt="" />
                  </div>
                </div>
              </div>
            ))}
          </Carousel>

          <div className="category">
            {categories.map(item => (
              <Link to={`/danh-muc/${item.id}`} href="/" className="category__item" key={item.id}>
                <img className="category-icon" src={item.imageUrl} alt="" />
                <p className="category-label">{item.name}</p>
              </Link>
            ))}
          </div>

          {flashSale?.items?.length > 0 && (
            <div className="sale">
              <div className="sale__header">
                <div className="sale__header__content">
                  <div className="sale__header__title">
                    <AiOutlineThunderbolt />
                    <i>FLASH SALE</i>
                  </div>
                  <div className="sale__header__countdown">
                    <span className="sale__header__countdown__text">KẾT THÚC SAU:</span>
                    {timeLeft ? (
                      <>
                        <span className="sale__header__countdown__number">
                          {format(timeLeft.hours)}
                        </span>
                        <b>:</b>
                        <span className="sale__header__countdown__number">
                          {format(timeLeft.minutes)}
                        </span>
                        <b>:</b>
                        <span className="sale__header__countdown__number">
                          {format(timeLeft.seconds)}
                        </span>
                      </>
                    ) : (
                      <span>--:--:--</span>
                    )}
                  </div>
                </div>
                {/* <a href="/" className="sale__header__view-all">
                Xem tất cả >
                </a> */}
              </div>
              <div className="sale__list">
                {flashSale?.items?.map(item => {
                  const percent = Math.round(
                    (1 - item.flashPrice / item.originalPrice) * 100
                  );

                  return (
                    <Link to={`/san-pham/${item.productId}`} className="sale__item" key={item.productId}>
                      <div className="sale__item__badge">-{percent}%</div>

                      <img
                        src={item.image}
                        alt=""
                        className="sale__item__image"
                      />

                      <div className="sale__item__title">
                        {item.name}
                      </div>

                      <div className="sale__item__price">
                        {item.flashPrice?.toLocaleString()}<sup>₫</sup>
                      </div>

                      <div className="sale__item__price--old">
                        {item.originalPrice?.toLocaleString()}<sup>₫</sup>
                      </div>
                    </Link>
                  );
                })}
              </div>
            </div>
          )}

          <div className="recommend">
            <div className="recommend__header">
              GỢI Ý HÔM NAY
            </div>
            <div className="recommend__list">
              <Link to="/" className="recommend__item">
                <img src="https://salt.tikicdn.com/cache/750x750/ts/product/55/d8/59/6ab171f91b1f5cddb98696a937f88ac5.jpg.webp" alt="" className="recommend__item__image" />
                <div className="recommend__item__info">
                  {/* <div className="recommend__item__rating">
                    <CiStar /> 
                    <b>4.8</b> <span>(32 đánh giá)</span>
                  </div> */}
                  <div className="recommend__item__title">
                    Cáp sạc nhanh, truyền dữ liệu tốc độ cao
                  </div>
                  <div className="recommend__item__footer">
                    <div className="recommend__item__price">
                      119.000<sup>₫</sup>
                    </div>
                    <div className="recommend__item__sold">
                      Đã bán 123
                    </div>
                  </div>
                </div>
              </Link>
              
            </div>
          </div>

          {/* <button className="button">Xem thêm</button> */}
        </div>
      </div>
    </>
  )
}

export default Home;