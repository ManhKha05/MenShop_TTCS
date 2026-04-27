from fastapi import APIRouter, HTTPException, Request
from app.schemas.payload import RecommendRequest

from app.models.recommendation.hybrid_recommender import get_detail_recs, get_home_recs

router = APIRouter()

@router.post("/detail")
def recommend_detail(req: RecommendRequest, request: Request):
    if req.target_product_id is None:
        raise HTTPException(status_code=400,
                            detail="Thiếu target_product_id.")

    state = request.app.state

    recs = get_detail_recs(
        user_id=req.user_id,
        target_id=req.target_product_id,
        product_list=state.product_list,
        product_dict=state.product_dict,
        idx_map=state.idx_map,
        cosine_matrix=state.cosine_sim,
        cf_model=state.cf_model,
        user_item_matrix=state.user_item_matrix,
        u_map=state.u_map,
        p_map=state.p_map,
        inv_u_map=state.inv_u_map,
        top_n=8
    )
    clean_recs = [int(item["product_id"]) for item in recs]
    return {"status": "success", "message": "Lấy gợi ý Detail thành công", "data": clean_recs}

@router.post("/home")
def recommend_home(req: RecommendRequest, request: Request):
    viewed_ids = req.viewed_product_ids if req.viewed_product_ids else []
    state = request.app.state

    recs = get_home_recs(
        user_id=req.user_id,
        viewed_ids=viewed_ids,
        product_list=state.product_list,
        product_dict=state.product_dict,
        idx_map=state.idx_map,
        cosine_matrix=state.cosine_sim,
        cf_model=state.cf_model,
        user_item_matrix=state.user_item_matrix,
        u_map=state.u_map,
        p_map=state.p_map,
        inv_u_map=state.inv_u_map,
        top_n=20
    )
    clean_recs = [int(item["product_id"]) for item in recs]
    return {"status": "success", "message": "Lấy gợi ý Home thành công", "data": clean_recs}