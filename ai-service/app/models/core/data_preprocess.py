import json
import re

def tokenize(text):
    return re.findall(r'\b\w\w+\b', text.lower())

def extract_and_combine(row):
    product_name = str(row.get('name', ''))
    description = str(row.get('description', ''))

    attr_data = row.get('attributes_json', {})
    json_text = ""

    if isinstance(attr_data, str):
        try:
            attr_data = json.loads(attr_data)
        except Exception:
            attr_data = {}

    if isinstance(attr_data, dict):
        json_text = " ".join(str(v) for v in attr_data.values())

    combined = f"{product_name} {description} {json_text}"

    cleaned_words_list = tokenize(combined)

    return " ".join(cleaned_words_list)