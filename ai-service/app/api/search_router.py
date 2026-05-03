from fastapi import APIRouter, Request
from app.schemas.payload import SearchRequest
from app.models.search.search_setup import text_to_vector

router = APIRouter()

@router.post("/vectorize")
def vectorize_text(req: SearchRequest, request: Request):
    state = request.app.state

    vector_data = text_to_vector(
        text=req.text,
        model=state.ai_model
    )

    return {
        "status": "success",
        "message": "Trích xuất vector ngữ nghĩa thành công",
        "vector": vector_data
    }