import numpy as np
from embedding_model import get_embedding_model

model = get_embedding_model()

texts = [
    "Nhiệm vụ, quyền hạn của Bộ Tư pháp",
    "4. Bộ trưởng Bộ Tư pháp có trách nhiệm tổ chức thi hành Nghị định này.\n",
    "Người sử dụng lao động có nghĩa vụ trả lương đúng hạn cho người lao động."
]

embeddings = model.encode(texts, normalize_embeddings=True)

sim_phaply = np.dot(embeddings[0], embeddings[1])
sim_khacbiet = np.dot(embeddings[0], embeddings[2])

print("Vector shape:", embeddings.shape)
print("Tương đồng giữa 2 câu cùng chủ đề 'Bộ Tư pháp':", sim_phaply)
print("Tương đồng giữa câu 'Bộ Tư pháp' và câu 'Lương lao động':", sim_khacbiet)
