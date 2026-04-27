import os
import json

# Tối ưu luồng CPU
os.environ['MKL_NUM_THREADS'] = '1'
os.environ['OPENBLAS_NUM_THREADS'] = '1'

from app.models.core.db_helper import DatabaseHelper
from app.models.core.data_preprocess import extract_and_combine
from app.models.recommendation.cbf_engine import calculate_tfidf_matrix, build_cosine_matrix
from app.models.recommendation.cf_engine import train_als_model_from_df

from app.models.recommendation.hybrid_recommender import get_home_recs

db_helper = DatabaseHelper()


def get_child_category_robust(attr_data):
    if not attr_data:
        return ""
    try:
        attrs = json.loads(attr_data) if isinstance(attr_data, str) else attr_data
        return str(attrs.get('Danh Mục', '')).strip()
    except Exception:
        return ""

def prepare_data_and_split():
    print("--- 1. Đọc dữ liệu Sản phẩm & Tiền xử lý (Từ MySQL) ---")

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

    print("--- 2. Xây dựng Ma trận Nội dung (CBF) ---")
    _, tfidf_cosine_sim = calculate_tfidf_matrix(df_products['clean_text'])
    cosine_sim = build_cosine_matrix(tfidf_cosine_sim)

    print("--- 3. Đọc dữ liệu Tương tác & Chia tập Train/Test (80/20) ---")
    df_interactions = db_helper.fetch_interactions_for_cf()

    if df_interactions.empty:
        raise ValueError("Không có dữ liệu tương tác để test!")

    df_interactions = df_interactions.sort_values(by=['user_id', 'created_at'])

    user_counts = df_interactions['user_id'].value_counts()
    valid_users = user_counts[user_counts >= 5].index
    df_valid = df_interactions[df_interactions['user_id'].isin(valid_users)]

    df_valid['rn'] = df_valid.groupby('user_id').cumcount()
    df_valid['total_count'] = df_valid.groupby('user_id')['user_id'].transform('count')

    train_df = df_valid[df_valid['rn'] < (df_valid['total_count'] * 0.8)].drop(
        columns=['rn', 'total_count']).reset_index(drop=True)
    test_df = df_valid[df_valid['rn'] >= (df_valid['total_count'] * 0.8)].drop(
        columns=['rn', 'total_count']).reset_index(drop=True)

    print("--- 4. Đang huấn luyện Model ALS trên tập Train ---")
    cf_model, user_item_matrix, u_map, p_map, inv_u_map = train_als_model_from_df(train_df)

    return product_list, product_dict, idx_map, cosine_sim, cf_model, user_item_matrix, u_map, p_map, inv_u_map, train_df, test_df


def evaluate_model(product_list, product_dict, idx_map, cosine_sim, cf_model, user_item_matrix, u_map, p_map, inv_u_map,
                   train_df, test_df, k=20):
    print(f"\n--- 5. BẮT ĐẦU CHẤM ĐIỂM (TOP {k} GỢI Ý) ---")

    models = {
        "Content-Based (CBF)": {"w_cbf": 1.0, "w_cf": 0.0},
        "Collaborative (CF)": {"w_cbf": 0.0, "w_cf": 1.0},
        "Hybrid (Lai)": {"w_cbf": 0.5, "w_cf": 0.5}
    }

    results = {}
    users_to_test = test_df['user_id'].unique()

    for model_name, weights in models.items():
        total_precision = 0.0
        total_recall = 0.0

        for uid in users_to_test:
            user_history = train_df[train_df['user_id'] == uid]['product_id'].tolist()
            actual_items = set(test_df[test_df['user_id'] == uid]['product_id'].tolist())

            recs = get_home_recs(
                user_id=uid,
                viewed_ids=user_history,
                product_list=product_list,
                product_dict=product_dict,
                idx_map=idx_map,
                cosine_matrix=cosine_sim,
                cf_model=cf_model,
                user_item_matrix=user_item_matrix,
                u_map=u_map,
                p_map=p_map,
                inv_u_map=inv_u_map,
                top_n=k,
                weight_cbf=weights['w_cbf'],
                weight_cf=weights['w_cf']
            )

            predicted_items = set([r['product_id'] for r in recs])

            hits = len(predicted_items.intersection(actual_items))

            precision = hits / k if k > 0 else 0
            recall = hits / len(actual_items) if len(actual_items) > 0 else 0

            total_precision += precision
            total_recall += recall

        num_users = len(users_to_test)
        if num_users > 0:
            results[model_name] = {
                "Precision": round((total_precision / num_users) * 100, 2),
                "Recall": round((total_recall / num_users) * 100, 2)
            }
        else:
            results[model_name] = {"Precision": 0, "Recall": 0}

    return results


if __name__ == "__main__":
    prod_list, prod_dict, idx_map, cos_sim, model_cf, user_item_matrix, map_u, map_p, inv_map_u, df_train, df_test = prepare_data_and_split()

    final_scores = evaluate_model(
        prod_list, prod_dict, idx_map, cos_sim, model_cf, user_item_matrix, map_u, map_p, inv_map_u, df_train, df_test, k=20
    )

    print("\n" + "=" * 45)
    print("KẾT QUẢ ĐÁNH GIÁ MÔ HÌNH (OFFLINE EVALUATION)")
    print("=" * 45)
    for name, score in final_scores.items():
        print(f"🔹 {name}:")
        print(f"   - Độ chính xác (Precision@{20}): {score['Precision']}%")
        print(f"   - Độ phủ (Recall@{20}):       {score['Recall']}%\n")
