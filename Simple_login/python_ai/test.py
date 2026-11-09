import numpy as np
from sentence_transformers import SentenceTransformer

model = SentenceTransformer(
    "paraphrase-multilingual-mpnet-base-v2"
)

texts = [
    "Nhiệm vụ của Bộ Tư pháp",
    "Nhiệm vụ của Sở Tư pháp",
]

embeddings = model.encode(texts, normalize_embeddings=True)

similarity = np.dot(embeddings[0], embeddings[1])

print("Cosine similarity:", similarity)