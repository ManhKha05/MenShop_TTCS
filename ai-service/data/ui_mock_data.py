import random
from datetime import datetime, timedelta

def generate_smart_interactions(num_users=150, target_records=5000):
    # Định nghĩa các tập ID sản phẩm theo từng Persona
    PERSONAS = {
        "OFFICE": [range(161, 181), range(241, 261), range(21, 41), range(261, 281), range(351, 371)],
        # Sơ mi, Vest, Blazer, Quần Âu, Kaki
        "SPORTY": [range(221, 241), range(331, 351), range(371, 391), range(1, 21), range(41, 61)],
        # Thể thao, Jogger, Short, Ba lỗ, Chống nắng
        "STREETWEAR": [range(61, 81), range(311, 331), range(291, 311), range(141, 161)],
        # Hoodie, Jeans, Cargo, Áo nỉ
        "CASUAL": [range(181, 201), range(201, 221), range(281, 291), range(391, 401)],
        # Thun cơ bản, Polo, Boxer, Tam giác
        "WINTER": [range(101, 121), range(81, 101), range(121, 141)]  # Phao, Gió, Len
    }

    persona_keys = list(PERSONAS.keys())
    data = []

    print(f"--- Đang tạo khoảng {target_records} tương tác mô phỏng hành vi thực tế ---")

    while len(data) < target_records:
        u_id = random.randint(1, num_users)
        user_persona = persona_keys[u_id % 5]
        if random.random() < 0.85:
            chosen_range = random.choice(PERSONAS[user_persona])
            p_id = random.choice(chosen_range)
        else:
            p_id = random.randint(1, 400)  # Random toàn bộ bảng

        base_days_ago = random.randint(1, 30)
        base_time = datetime.now() - timedelta(days=base_days_ago, hours=random.randint(0, 23))

        data.append((u_id, p_id, 'VIEW', 1.0, base_time.strftime('%Y-%m-%d %H:%M:%S')))

        action_chance = random.random()

        if action_chance < 0.10:
            buy_time = base_time + timedelta(minutes=random.randint(2, 15))
            data.append((u_id, p_id, 'PURCHASE', 5.0, buy_time.strftime('%Y-%m-%d %H:%M:%S')))

        elif action_chance < 0.35:
            cart_time = base_time + timedelta(minutes=random.randint(2, 30))
            data.append((u_id, p_id, 'ADD_TO_CART', 3.0, cart_time.strftime('%Y-%m-%d %H:%M:%S')))

            if random.random() < 0.40:
                buy_time = cart_time + timedelta(hours=random.randint(1, 48))
                data.append((u_id, p_id, 'PURCHASE', 5.0, buy_time.strftime('%Y-%m-%d %H:%M:%S')))

    data = data[:target_records]

    with open('insert_eval_interactions.sql', 'w', encoding='utf-8') as f:
        f.write("USE ttcs;\n")
        f.write("SET FOREIGN_KEY_CHECKS = 0;\n\n")
        f.write("TRUNCATE TABLE user_interaction;\n\n")

        chunk_size = 1000
        for i in range(0, len(data), chunk_size):
            chunk = data[i:i + chunk_size]
            f.write(
                "INSERT INTO user_interaction (user_id, product_id, interaction_type, weight_score, created_at) VALUES \n")
            lines = [f"({u}, {p}, '{a}', {w}, '{t}')" for u, p, a, w, t in chunk]
            f.write(",\n".join(lines) + ";\n\n")

        f.write("SET FOREIGN_KEY_CHECKS = 1;\n")

    print(f"Đã tạo xong file 'insert_eval_interactions.sql' với {len(data)} dòng!")
    print(f"Cấu trúc Persona: {persona_keys}")

if __name__ == "__main__":
    generate_smart_interactions(num_users=150, target_records=5000)