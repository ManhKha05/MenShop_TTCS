import math

# Hàm tính tần suất xuất hiện của 1 từ trong 1 tài liệu
def calculate_tf(document):
    words = document.split()

    word_count = {}
    for word in words:
        word_count[word] = word_count.get(word, 0) + 1

    return word_count

# Tính toán mức độ quan trọng của 1 từ trong toàn bộ kho tài liệu
def calculate_idf(all_documents):
    total_documents = len(all_documents)
    idf_dict = {}

    # Đếm xem mỗi từ xuất hiện trong bao nhiêu tài liệu
    for document in all_documents:
        # Dùng set để tránh lặp từ trong 1 tài liệu
        words_in_document = set(document.split())
        for word in words_in_document:
            idf_dict[word] = idf_dict.get(word, 0) + 1

    # Tính IDF: ln((1 + N) / (1 + df)) + 1
    for word, doc_freq in idf_dict.items():
        idf_dict[word] = math.log((1 + total_documents) / (1 + doc_freq)) + 1.0

    return idf_dict

def calculate_tfidf_matrix(data):
    # Tính IDF cho toàn bộ kho dữ liệu
    idf_scores = calculate_idf(data)

    # Tạo từ điển chung
    vocabulary = set()
    for document in data:
        vocabulary.update(document.split())
    vocabulary = sorted(list(vocabulary))

    # Tính ma trận TF-IDF
    tfidf_matrix = []

    for document in data:
        tf_scores = calculate_tf(document)
        sum_sq = 0.0

        # Tạo vector có độ dài bằng len(vocabulary) cho mỗi doc
        doc_vector = []
        for word in vocabulary:
            tfidf_value = tf_scores.get(word, 0.0) * idf_scores.get(word, 0.0)
            doc_vector.append(float(tfidf_value))
            sum_sq += tfidf_value ** 2

        # Chuẩn hóa L2
        magnitude = math.sqrt(sum_sq)
        if magnitude > 0:
            doc_vector = [v / magnitude for v in doc_vector]

        tfidf_matrix.append(doc_vector)

    return vocabulary, tfidf_matrix

def build_cosine_matrix(tfidf_matrix):
    # Tạo ma trận Cosine cho toàn bộ kho hàng
    num_docs = len(tfidf_matrix)
    cosine_matrix = [[0.0 for _ in range(num_docs)] for _ in range(num_docs)]

    for i in range(num_docs):
        cosine_matrix[i][i] = 1.0
        for j in range(i + 1, num_docs):
            score = sum(float(v1) * float(v2) for v1, v2 in zip(tfidf_matrix[i], tfidf_matrix[j]))
            score = round(score, 4)

            cosine_matrix[i][j] = score
            cosine_matrix[j][i] = score

    return cosine_matrix

