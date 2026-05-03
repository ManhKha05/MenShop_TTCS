from sentence_transformers import SentenceTransformer

def load_ai_model():
    print("Đang tải model BGE-M3 (1024 chiều) cho Smart Search...")
    model = SentenceTransformer('BAAI/bge-m3')
    return model

def text_to_vector(text: str, model):
    vector = model.encode(text)
    return vector.tolist()