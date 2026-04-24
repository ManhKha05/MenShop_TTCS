import "./Review.scss";
import {
  Card,
  Rate,
  Tag,
  Select,
  Input,
  Row,
  Col,
  Empty,
  Spin,
  Pagination
} from "antd";
import { useEffect, useState } from "react";
import { get } from "../../../utils/request";
import {formatDateTime2} from "../../../utils/date"

const { Option } = Select;

function ShopReview() {
  const [loading, setLoading] = useState(true);
  const [reviews, setReviews] = useState([]);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);

  const [filter, setFilter] = useState({
    rating: null,
    keyword: "",
    sort: "newest"
  });

  const fetchReviews = async () => {
    try {
      setLoading(true);

      const res = await get("shop/reviews", {
        page: page - 1,
        size: 5,
        rating: filter.rating,
        keyword: filter.keyword,
        sort: filter.sort
      });

      const data = await res.json();

      setReviews(data.content);
      setTotal(data.totalElements);
    } catch (err) {
      console.error("Lỗi load review", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [page, filter]);

  return (
    <div className="shop-review">
      <div className="shop-review__header">
        <div className="shop-review__title">Đánh giá sản phẩm</div>

        <div className="shop-review__filter">
          <Select
            placeholder="Lọc theo sao"
            style={{ width: 160 }}
            allowClear
            onChange={(value) => setFilter({ ...filter, rating: value })}
          >
            <Option value={5}>5 sao</Option>
            <Option value={4}>4 sao</Option>
            <Option value={3}>3 sao</Option>
            <Option value={2}>1-2 sao</Option>
          </Select>

          <Select
            value={filter.sort}
            style={{ width: 160 }}
            onChange={(value) => setFilter({ ...filter, sort: value })}
          >
            <Option value="newest">Mới nhất</Option>
            <Option value="rating">Rating cao</Option>
          </Select>

          <Input.Search
            placeholder="Tìm sản phẩm"
            onSearch={(value) => setFilter({ ...filter, keyword: value })}
          />
        </div>
      </div>

      {loading ? (
        <Spin />
      ) : reviews?.length === 0 ? (
        <Empty description="Chưa có đánh giá" />
      ) : (
        <>
          {reviews.map((review) => (
            <Card key={review.id} className="shop-review__item">
              <Row gutter={16}>
                <Col span={4}>
                  <img src={review.productImage} alt="" className="shop-review__product-img" />
                </Col>

                <Col span={20}>
                  <div className="shop-review__top">
                    <div className="shop-review__user">
                      <img src={review.avatar} className="avatar" />
                      <span>{review.fullName}</span>
                    </div>

                    <Rate disabled value={review.rating} />
                  </div>

                  <div className="shop-review__product">
                    {review.productName}
                  </div>

                  <div className="shop-review__content">
                    {review.comment}
                  </div>

                  {review.images?.length > 0 && (
                    <div className="shop-review__images">
                      {review.images.map((img, i) => (
                        <img key={i} src={img} />
                      ))}
                    </div>
                  )}

                  <div className="shop-review__bottom">
                    <Tag color="blue">{formatDateTime2(review.createdAt)}</Tag>
                  </div>
                </Col>
              </Row>
            </Card>
          ))}

          <Pagination
            current={page}
            total={total}
            pageSize={5}
            onChange={setPage}
          />
        </>
      )}
    </div>
  );
}

export default ShopReview;