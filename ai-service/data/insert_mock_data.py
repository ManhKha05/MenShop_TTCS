import pandas as pd
import re
import random
import json
import ast

file_path = r'D:\Python\ai-service\data\raw\Data - Hỗ trợ nhập liệu sản phẩm thời trang.csv'
df = pd.read_csv(file_path)

users_data = [(1, 'Admin Shop', 'admin', 'admin@gmail.com')]
brands_data = []
shops_data = []
categories_data = []  
products_data = []

brands_map = {}
categories_map = {}

brand_shop_id_counter = 1
category_id_counter = 1
product_id_counter = 1

for index, row in df.iterrows():
    # Bỏ qua nếu không có tên sản phẩm
    if 'Tên sản phẩm' not in row or pd.isna(row['Tên sản phẩm']):
        # Nếu CSV của bạn không có cột 'Tên sản phẩm' mà là tên khác, hãy sửa lại ở đây
        continue

    # --- Làm sạch và xử lý giá ---
    price_raw = str(row['Giá']) if 'Giá' in row and pd.notna(row['Giá']) else '0'
    price_clean = re.sub(r'[^\d]', '', price_raw)
    price = float(price_clean) if price_clean else 0
    sale_price = round(price * 0.9, 2)

    # --- Xử lý văn bản ---
    name = str(row['Tên sản phẩm']).replace("'", "''")
    brand_name = str(row['Thương hiệu']).replace("'", "''") if 'Thương hiệu' in row and pd.notna(
        row['Thương hiệu']) else 'No Brand'
    description = str(row['Mô tả']).replace("'", "''") if 'Mô tả' in df.columns and pd.notna(row['Mô tả']) else ""

    # --- Xử lý Brand & Shop ---
    if brand_name not in brands_map:
        brands_map[brand_name] = brand_shop_id_counter
        brands_data.append((brand_shop_id_counter, brand_name))
        shops_data.append((brand_shop_id_counter, 1, brand_name, 'ACTIVE'))
        brand_shop_id_counter += 1

    b_id = brands_map[brand_name]
    shop_id = b_id

    parent_cat_name = str(row['Danh Mục']).strip().replace("'", "''") if 'Danh Mục' in row and pd.notna(
        row['Danh Mục']) else 'Uncategorized'

    if parent_cat_name not in categories_map:
        categories_map[parent_cat_name] = category_id_counter
        # Thêm danh mục cha với parent_id = NULL
        categories_data.append((category_id_counter, parent_cat_name, None))
        category_id_counter += 1

    parent_id = categories_map[parent_cat_name]

    # Mặc định sản phẩm sẽ dùng ID của danh mục cha (đề phòng JSON lỗi hoặc không có con)
    product_category_id = parent_id

    raw_json = row['JSON Attributes'] if 'JSON Attributes' in row else None
    attr_json_str = '{}'
    child_cat_name = None

    if pd.notna(raw_json) and str(raw_json).strip() != '':
        attr_json_str = str(raw_json).replace("'", "''")
        try:
            json_dict = json.loads(str(raw_json))
        except json.JSONDecodeError:
            try:
                json_dict = ast.literal_eval(str(raw_json))
            except Exception:
                json_dict = {}

        if isinstance(json_dict, dict):
            # Lấy danh mục con bằng chính key "Danh Mục" trong chuỗi JSON
            raw_child_name = json_dict.get('Danh Mục')
            if raw_child_name:
                child_cat_name = str(raw_child_name).strip().replace("'", "''")

    if child_cat_name:
        if child_cat_name.lower() == parent_cat_name.lower():
            # Nếu trùng tên -> Không tạo danh mục con.
            pass
        else:
            # Nếu khác tên -> Tạo danh mục con
            child_path_key = f"{parent_cat_name} -> {child_cat_name}"

            if child_path_key not in categories_map:
                categories_map[child_path_key] = category_id_counter
                # Insert danh mục con với parent_id = id của danh mục cha
                categories_data.append((category_id_counter, child_cat_name, parent_id))
                category_id_counter += 1

            # Gán ID của danh mục con cho sản phẩm
            product_category_id = categories_map[child_path_key]

    # --- Giả lập dữ liệu cho AI ---
    view_count = random.randint(100, 5000)
    sold_count = random.randint(0, 500)
    rating_avg = round(random.uniform(3.5, 5.0), 2)
    status = 'ACTIVE'

    # --- Xử lý Product ---
    products_data.append((
        product_id_counter, shop_id, product_category_id, b_id, name,
        price, sale_price, description, attr_json_str,
        view_count, sold_count, rating_avg, status
    ))
    product_id_counter += 1

def generate_bulk_insert_sql(table_name, columns, data_list):
    if not data_list: return ""
    sql = f"INSERT INTO {table_name} ({columns}) VALUES \n"
    values_str = []
    for item in data_list:
        formatted_parts = []
        for x in item:
            if x is None:
                formatted_parts.append("NULL")
            elif isinstance(x, str):
                formatted_parts.append(f"'{x}'")
            else:
                formatted_parts.append(str(x))
        values_str.append(f"({', '.join(formatted_parts)})")
    return sql + ",\n".join(values_str) + ";\n\n"

with open('insert_product_data.sql', 'w', encoding='utf-8') as f:
    f.write("USE ttcs;\n")
    f.write("SET FOREIGN_KEY_CHECKS = 0;\n\n")

    f.write(generate_bulk_insert_sql('user', 'id, full_name, username, email', users_data))
    f.write(generate_bulk_insert_sql('brand', 'id, name', brands_data))
    f.write(generate_bulk_insert_sql('shop', 'id, user_id, name, status', shops_data))

    f.write(generate_bulk_insert_sql('category', 'id, name, parent_id', categories_data))

    product_cols = 'id, shop_id, category_id, brand_id, name, price, sale_price, description, attributes_json, view_count, sold_count, rating_avg, status'
    f.write(generate_bulk_insert_sql('product', product_cols, products_data))

    f.write("SET FOREIGN_KEY_CHECKS = 1;\n")

print(f"Đã tạo xong!")