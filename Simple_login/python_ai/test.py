import numpy as np
from embedding_model import get_embedding_model

model = get_embedding_model()

texts = [
    "nhiệm vụ của bộ tư pháp",
    "2. Bộ Công an là cơ quan đầu mối giúp Chính phủ thực hiện thống nhất quản lý nhà nước về trật tự, an toàn giao thông đường bộ; xây dựng lực lượng Cảnh sát giao thông chính quy, tinh nhuệ, hiện đại đáp ứng yêu cầu nhiệm vụ bảo đảm trật tự, an toàn giao thông đường bộ.",
    "3. Bộ Công an chủ trì, phối hợp với các Bộ, ngành liên quan giúp Chính phủ đàm phán, ký kết điều ước quốc tế, thỏa thuận quốc tế hoặc đề xuất cấp có thẩm quyền ký kết điều ước quốc tế, thỏa thuận quốc tế về trật tự, an toàn giao thông đường bộ; chủ trì, phối hợp với các Bộ, ngành liên quan thực hiện hợp tác quốc tế về trật tự, an toàn giao thông đường bộ.",
    "QUẢN LÝ NHÀ NƯỚC VỀ HÔN NHÂN VÀ GIA ĐÌNH CÓ YẾU TỐ NƯỚC NGOÀI",
    "Điều 36. Nhiệm vụ, quyền hạn của Bộ Tư pháp\nBộ Tư pháp chịu trách nhiệm trước Chính phủ thống nhất quản lý nhà nước về hôn nhân và gia đình có yếu tố nước ngoài trong phạm vi toàn quốc, có nhiệm vụ và quyền hạn sau đây:"
]

embeddings = model.encode(texts, normalize_embeddings=True)

sim_2 = np.dot(embeddings[0], embeddings[1])
sim_3 = np.dot(embeddings[0], embeddings[2])
sim_87 = np.dot(embeddings[0],embeddings[3])
sim_36 = np.dot(embeddings[0],embeddings[4])


print("Vector shape:", embeddings.shape)
print(f"{sim_2} _ {sim_3} _ {sim_87} _ {sim_36}")
