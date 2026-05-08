from fastapi import FastAPI
import uvicorn
from contextlib import asynccontextmanager
from app.api import rec_router
from app.api import search_router
from app.models.recommendation.rec_setup import load_and_train_models
from app.models.search.search_setup import load_ai_model

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Đang khởi động Server AI và nạp dữ liệu từ MySQL...")
    try:
        (product_list, product_dict, idx_map, cosine_sim,
         cf_model, user_item_matrix, u_map, p_map, inv_u_map) = load_and_train_models()

        app.state.product_list = product_list
        app.state.product_dict = product_dict
        app.state.idx_map = idx_map
        app.state.cosine_sim = cosine_sim
        app.state.cf_model = cf_model
        app.state.user_item_matrix = user_item_matrix
        app.state.u_map = u_map
        app.state.p_map = p_map
        app.state.inv_u_map = inv_u_map

        ai_model = load_ai_model()
        app.state.ai_model = ai_model

        print("Server AI đã khởi động xong. Sẵn sàng nhận Request ở Cổng 8000!")
    except Exception as e:
        print(f"Lỗi khởi động hệ thống: {str(e)}")

    yield

    print("Đang tắt Server AI và dọn dẹp RAM...")

app = FastAPI(
    title="E-commerce AI Engine",
    description="Microservice cho Smart Search & Recommendation",
    version="2.0.0",
    lifespan=lifespan
)

app.include_router(rec_router.router, prefix="/api/recommend", tags=["Recommendation"])
app.include_router(search_router.router, prefix="/api/search", tags=["Smart Search"])

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="127.0.0.1", port=8000, reload=True)