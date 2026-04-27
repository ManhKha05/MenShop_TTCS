import scipy.sparse as sparse
import implicit

def train_als_model_from_df(df):
    df['user_id'] = df['user_id'].astype("category")
    df['product_id'] = df['product_id'].astype("category")

    user_map = dict(enumerate(df['user_id'].cat.categories))
    product_map = dict(enumerate(df['product_id'].cat.categories))
    inv_user_map = {v: k for k, v in user_map.items()}

    user_item_matrix = sparse.coo_matrix((
        df['weight'].astype(float),
        (df['user_id'].cat.codes, df['product_id'].cat.codes) # (Row=User, Col=Item)
    )).tocsr()

    model = implicit.als.AlternatingLeastSquares(
        factors=16,
        regularization=0.1,
        iterations=20,
        alpha=20,
        random_state=42,
        use_native=True
    )

    print(f"--- Đang huấn luyện ALS với {len(df)} tương tác ---")
    model.fit(user_item_matrix, show_progress=False)

    return model, user_item_matrix, user_map, product_map, inv_user_map


def get_cf_recommendations(user_id_orig, model, user_item_matrix, user_map, product_map, inv_user_map, n=20):
    if user_id_orig not in inv_user_map:
        return []

    user_idx = inv_user_map[user_id_orig]

    ids, scores = model.recommend(
        user_idx,
        user_item_matrix,
        N=n,
        filter_already_liked_items=False
    )

    recs = []
    for i, score in zip(ids, scores):
        real_product_id = product_map.get(int(i))
        if real_product_id is not None:
            recs.append({
                "product_id": int(real_product_id),
                "cf_score": round(float(score), 4)
            })

    return recs