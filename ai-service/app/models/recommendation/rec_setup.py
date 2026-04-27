import os
import json

os.environ['MKL_NUM_THREADS'] = '1'
os.environ['OPENBLAS_NUM_THREADS'] = '1'

from app.models.core.db_helper import DatabaseHelper
from app.models.core.data_preprocess import extract_and_combine
from app.models.recommendation.cbf_engine import calculate_tfidf_matrix, build_cosine_matrix
from app.models.recommendation.cf_engine import train_als_model_from_df

db_helper = DatabaseHelper()

def get_child_category_robust(attr_data):
    if not attr_data:
        return ""
    try:
        attrs = json.loads(attr_data) if isinstance(attr_data, str) else attr_data
        return str(attrs.get('Danh Mục', '')).strip()
    except Exception:
        return ""

def load_and_train_models():
    print("--- [1/3] Đọc dữ liệu Sản phẩm từ MySQL ---")
    df_products = db_helper.fetch_products_for_ai()

    if df_products.empty:
        raise ValueError("Database không có sản phẩm nào ở trạng thái ACTIVE!")

    df_products['child_category'] = df_products['attributes_json'].apply(get_child_category_robust)
    df_products['clean_text'] = df_products.apply(extract_and_combine, axis=1)

    product_list = df_products[['id', 'name', 'category_id', 'child_category']].rename(
        columns={'category_id': 'parent_category'}
    ).to_dict('records')

    product_dict = {p['id']: p for p in product_list}
    idx_map = {p['id']: idx for idx, p in enumerate(product_list)}

    print("--- [2/3] Xây dựng Ma trận Nội dung (CBF) ---")
    _, tfidf_cosine_sim = calculate_tfidf_matrix(df_products['clean_text'])
    cosine_sim = build_cosine_matrix(tfidf_cosine_sim)

    print("--- [3/3] Huấn luyện Mô hình Đám đông (CF - ALS) ---")
    df_interactions = db_helper.fetch_interactions_for_cf()

    if df_interactions.empty:
        print("Cảnh báo: Chưa có dữ liệu tương tác người dùng.")
        cf_model, user_item_matrix, u_map, p_map, inv_u_map = None, None, {}, {}, {}
    else:
        # Nhận thêm inv_u_map từ cf_engine đã refactor
        cf_model, user_item_matrix, u_map, p_map, inv_u_map = train_als_model_from_df(df_interactions)

    print("HỆ THỐNG AI ĐÃ SẴN SÀNG TRONG RAM!\n")
    return product_list, product_dict, idx_map, cosine_sim, cf_model, user_item_matrix, u_map, p_map, inv_u_map
