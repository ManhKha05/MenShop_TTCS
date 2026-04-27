from app.models.recommendation.cf_engine import get_cf_recommendations

def min_max_normalize(score_dict):
    """Kéo giãn điểm số về thang 0.0 -> 1.0"""
    if not score_dict: return {}
    scores = list(score_dict.values())
    min_score = min(scores)
    max_score = max(scores)

    if max_score == min_score:
        return {k: 1.0 for k in score_dict.keys()}

    return {k: (v - min_score) / (max_score - min_score) for k, v in score_dict.items()}


def get_detail_recs(user_id, target_id, product_list, product_dict, idx_map, cosine_matrix, cf_model, user_item_matrix, u_map, p_map, inv_u_map,
                    top_n=8, weight_cbf=0.7, weight_cf=0.3):
    """
    Gợi ý trang chi tiết: Ưu tiên SP tương đồng nội dung (CBF) + Sở thích cá nhân (CF)
    """

    if target_id not in idx_map:
        return []

    target_idx = idx_map[target_id]
    target_info = product_dict[target_id]
    target_parent = target_info.get('parent_category')
    target_child = target_info.get('child_category')

    # --- 1. CBF Score ---
    row = cosine_matrix[target_idx]
    cbf_scores = {
        product_list[j]['id']: score
        for j, score in enumerate(row)
        if product_list[j]['id'] != target_id and score > 0
    }

    # --- 2. CF Score ---
    cf_recs = get_cf_recommendations(user_id, cf_model, user_item_matrix, u_map, p_map, inv_u_map, n=50)
    cf_scores = {r['product_id']: r['cf_score'] for r in cf_recs if r['product_id'] != target_id}

    # --- 3. Hybrid Logic ---
    cbf_norm = min_max_normalize(cbf_scores)
    cf_norm = min_max_normalize(cf_scores)

    all_candidates = set(cbf_norm.keys()) | set(cf_norm.keys())

    # Xử lý Cold Start cho CF
    actual_w_cf = weight_cf if cf_norm else 0.0
    actual_w_cbf = 1.0 - actual_w_cf

    hybrid_results = []
    for pid in all_candidates:
        score = (cbf_norm.get(pid, 0.0) * actual_w_cbf) + (cf_norm.get(pid, 0.0) * actual_w_cf)
        if score > 0.01:
            hybrid_results.append((pid, score))

    hybrid_results.sort(key=lambda x: x[1], reverse=True)

    recommendations = []
    existing_ids = {target_id}

    for pid, score in hybrid_results:
        p_info = product_dict.get(pid)
        if p_info:
            recommendations.append({
                "product_id": pid,
                "product_name": p_info['name'],
                "score": round(score, 4),
                "type": "Hybrid_Detail"
            })
            existing_ids.add(pid)
            if len(recommendations) >= top_n: break

    # --- 4. Waterfall Fallback ---
    for level, cat_val in [("Child", target_child), ("Parent", target_parent)]:
        if len(recommendations) < top_n and cat_val:
            for p in product_list:
                p_cat = p.get('child_category') if level == "Child" else p.get('parent_category')
                if p_cat == cat_val and p['id'] not in existing_ids:
                    recommendations.append({
                        "product_id": p['id'],
                        "product_name": p['name'],
                        "score": 0.0,
                        "type": f"{level}_Fallback"
                    })
                    existing_ids.add(p['id'])
                    if len(recommendations) >= top_n: break

    return recommendations

def get_home_recs(user_id, viewed_ids, product_list, product_dict, idx_map, cosine_matrix, cf_model, user_item_matrix, u_map, p_map, inv_u_map,
                  top_n=20, weight_cbf=0.5, weight_cf=0.5):
    """
    Gợi ý trang chủ: Ưu tiên hành vi đám đông (CF) + Lịch sử cá nhân (CBF)
    """
    viewed_ids_set = set(viewed_ids)

    cbf_scores = {}
    user_child_cats = set()
    user_parent_cats = set()

    # --- 1. CBF từ lịch sử xem ---
    for vid in viewed_ids_set:
        if vid in idx_map:
            idx = idx_map[vid]
            p_info = product_dict[vid]
            if p_info.get('child_category'): user_child_cats.add(p_info['child_category'])
            if p_info.get('parent_category'): user_parent_cats.add(p_info['parent_category'])

            for j, score in enumerate(cosine_matrix[idx]):
                pid = product_list[j]['id']
                if pid not in viewed_ids_set and score > 0:
                    cbf_scores[pid] = cbf_scores.get(pid, 0.0) + score

    # --- 2. CF ---
    cf_recs = get_cf_recommendations(user_id, cf_model, user_item_matrix, u_map, p_map, inv_u_map, n=100)
    cf_scores = {r['product_id']: r['cf_score'] for r in cf_recs if r['product_id'] not in viewed_ids_set}

    # --- 3. Hybrid ---
    cbf_norm = min_max_normalize(cbf_scores)
    cf_norm = min_max_normalize(cf_scores)

    all_pids = set(cbf_norm.keys()) | set(cf_norm.keys())
    actual_w_cf = weight_cf if cf_norm else 0.0
    actual_w_cbf = 1.0 - actual_w_cf

    hybrid_list = []
    for pid in all_pids:
        score = (cbf_norm.get(pid, 0.0) * actual_w_cbf) + (cf_norm.get(pid, 0.0) * actual_w_cf)
        if score > 0.01:
            hybrid_list.append((pid, score))

    hybrid_list.sort(key=lambda x: x[1], reverse=True)

    recommendations = []
    existing_ids = set(viewed_ids_set)

    for pid, score in hybrid_list:
        p_info = product_dict.get(pid)
        if p_info:
            recommendations.append({
                "product_id": pid,
                "product_name": p_info['name'],
                "score": round(score, 4),
                "type": "Hybrid_Home"
            })
            existing_ids.add(pid)
            if len(recommendations) >= top_n: break

    # --- 4. Waterfall Fallback ---
    if len(recommendations) < top_n:
        for p in product_list:
            if (p.get('child_category') in user_child_cats or p.get('parent_category') in user_parent_cats) \
                    and p['id'] not in existing_ids:
                recommendations.append({
                    "product_id": p['id'],
                    "product_name": p['name'],
                    "score": 0.0,
                    "type": "Category_Fallback"
                })
                existing_ids.add(p['id'])
                if len(recommendations) >= top_n: break

    return recommendations