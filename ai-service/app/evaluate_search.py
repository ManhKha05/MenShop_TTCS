import requests
import numpy as np

ground_truth_levels = {
    "LEVEL_1_EXACT": {
        "áo ba lỗ thể thao": [str(i) for i in range(1, 21)],
        "áo blazer nam": [str(i) for i in range(21, 41)],
        "áo chống nắng nam": [str(i) for i in range(41, 61)],
        "áo hoodie form rộng": [str(i) for i in range(61, 81)],
        "áo khoác gió cản nước": [str(i) for i in range(81, 101)],
        "áo khoác phao mùa đông": [str(i) for i in range(101, 121)],
        "áo len dệt kim": [str(i) for i in range(121, 141)],
        "áo nỉ nam cổ tròn": [str(i) for i in range(141, 161)],
        "áo sơ mi dài tay": [str(i) for i in range(161, 181)],
        "áo thun trơn basic": [str(i) for i in range(181, 201)],
        "áo thun polo có cổ": [str(i) for i in range(201, 221)],
        "áo thể thao tập gym": [str(i) for i in range(221, 241)],
        "áo vest nam công sở": [str(i) for i in range(241, 261)],
        "quần âu nam": [str(i) for i in range(261, 281)],
        "quần lót đùi boxer": [str(i) for i in range(281, 291)],
        "quần túi hộp cargo": [str(i) for i in range(291, 311)],
        "quần jean nam": [str(i) for i in range(311, 331)],
        "quần jogger bo gấu": [str(i) for i in range(331, 351)],
        "quần kaki ống đứng": [str(i) for i in range(351, 371)],
        "quần short ngang gối": [str(i) for i in range(371, 391)],
        "quần lót tam giác": [str(i) for i in range(391, 401)]
    }
}

# ground_truth_levels = {
#     "LEVEL_2_NOISY": {
#         "ao 3 lo mac nha": [str(i) for i in range(1, 21)],
#         "ao bo lay zơ nam": [str(i) for i in range(21, 41)],
#         "ao chong lang co mu": [str(i) for i in range(41, 61)],
#         "ao hu di rong": [str(i) for i in range(61, 81)],
#         "ao khoat dzo nhe": [str(i) for i in range(81, 101)],
#         "ao mut sieu nhe": [str(i) for i in range(101, 121)],
#         "ao lenn det kym": [str(i) for i in range(121, 141)],
#         "ao ny co tron": [str(i) for i in range(141, 161)],
#         "ao so my tay dai": [str(i) for i in range(161, 181)],
#         "ao phong tronn ba sic": [str(i) for i in range(181, 201)],
#         "ao po lo co co": [str(i) for i in range(201, 221)],
#         "ao the thao tap dym": [str(i) for i in range(221, 241)],
#         "ao vet nam cong so": [str(i) for i in range(241, 261)],
#         "quan au lam": [str(i) for i in range(261, 281)],
#         "quan xip dui nam": [str(i) for i in range(281, 291)],
#         "quan tui hup rong": [str(i) for i in range(291, 311)],
#         "wần bo ong rong": [str(i) for i in range(311, 331)],
#         "quan jo ger bo ong": [str(i) for i in range(331, 351)],
#         "quan ca cj ong dung": [str(i) for i in range(351, 371)],
#         "quan xot di bien": [str(i) for i in range(371, 391)],
#         "quan xi lip tam giac": [str(i) for i in range(391, 401)]
#     }
# }

# ground_truth_levels = {
#     "LEVEL_3_SEMANTIC": {
#         "đồ sát nách tập tạ": [str(i) for i in range(1, 21)],
#         "đồ khoác ngoài đi tiệc": [str(i) for i in range(21, 41)],
#         "áo che nắng gắt": [str(i) for i in range(41, 61)],
#         "đồ ấm chui đầu": [str(i) for i in range(61, 81)],
#         "áo che sương nhẹ": [str(i) for i in range(81, 101)],
#         "áo khoác đại hàn": [str(i) for i in range(101, 121)],
#         "đồ dệt kim mùa thu": [str(i) for i in range(121, 141)],
#         "áo chui đầu lót lông": [str(i) for i in range(141, 161)],
#         "áo đóng thùng lịch sự": [str(i) for i in range(161, 181)],
#         "áo phông lót trong": [str(i) for i in range(181, 201)],
#         "áo cộc tay đi làm": [str(i) for i in range(201, 221)],
#         "đồ mặc chạy bộ": [str(i) for i in range(221, 241)],
#         "đồ mặc dự đám cưới": [str(i) for i in range(241, 261)],
#         "quần đứng dáng công sở": [str(i) for i in range(261, 281)],
#         "đồ mặc trong ống vuông": [str(i) for i in range(281, 291)],
#         "quần hộp dạo phố": [str(i) for i in range(291, 311)],
#         "quần vải thô bụi bặm": [str(i) for i in range(311, 331)],
#         "quần chun vận động": [str(i) for i in range(331, 351)],
#         "quần đứng dáng không nhăn": [str(i) for i in range(351, 371)],
#         "đồ đùi mặc đi biển": [str(i) for i in range(371, 391)],
#         "đồ lót khoét hông": [str(i) for i in range(391, 401)]
#     }
# }

SPRING_BOOT_API = "http://localhost:8080/test/search"

def get_search_results(query, strategy, size=10):
    """Gọi API Spring Boot để lấy danh sách ID sản phẩm"""
    try:
        params = {"q": query, "strategy": strategy, "page": 0, "size": size}
        response = requests.get(SPRING_BOOT_API, params=params)

        if response.status_code == 200:
            data = response.json()
            return [str(item['id']) for item in data]
    except Exception as e:
        print(f"Lỗi gọi API: {e}")
    return []

def evaluate_level(queries_dict, strategy, k=10):
    """Đánh giá các chỉ số cho một tập query nhất định"""
    mrr_sum = 0
    precisions = []
    recalls = []

    for query, expected_ids in queries_dict.items():
        expected_set = set(expected_ids)
        retrieved_ids = get_search_results(query, strategy, k)

        # Tính P@K và R@K
        hits = len(set(retrieved_ids).intersection(expected_set))
        precisions.append(hits / k)
        recalls.append(hits / len(expected_set) if expected_set else 0)

        # Tính MRR
        rr = 0
        for rank, prod_id in enumerate(retrieved_ids, start=1):
            if prod_id in expected_set:
                rr = 1.0 / rank
                break
        mrr_sum += rr

    return {
        "P@5": f"{np.mean(precisions):.3f}",
        "R@5": f"{np.mean(recalls):.3f}",
        "MRR": f"{(mrr_sum / len(queries_dict)):.3f}"
    }

if __name__ == "__main__":
    print("\nKẾT QUẢ KIỂM THỬ A/B TESTING PHÂN CẤP (NGẮN 2-3 TỪ)\n")

    strategies = ["TEXT", "VECTOR", "HYBRID"]

    for level_name, queries in ground_truth_levels.items():
        print(f"{level_name.upper()} (Số lượng: {len(queries)} queries)")
        print(f"{'STRATEGY':<12} | {'PRECISION@5':<12} | {'RECALL@5':<10} | {'MRR':<10}")
        print("-" * 52)

        for strat in strategies:
            metrics = evaluate_level(queries, strat, k=10)
            print(f"{strat:<12} | {metrics['P@5']:<12} | {metrics['R@5']:<10} | {metrics['MRR']:<10}")
        print("\n" + "=" * 52 + "\n")

    retrieved_ids = get_search_results("quan rin nam", "HYBRID", 10)
    print(f"DEBUG: Kết quả lấy được: {retrieved_ids}")