from pydantic import BaseModel
from typing import List, Optional

class RecommendRequest(BaseModel):
    user_id: int
    target_product_id: Optional[int] = None
    viewed_product_ids: List[int] = []

class SearchRequest(BaseModel):
    text: str