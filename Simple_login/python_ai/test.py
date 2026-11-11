import numpy as np
from embedding_model import get_embedding_model

model = get_embedding_model()

texts = [
    "Nhiệm vụ, quyền hạn của Bộ Tư pháp",
    "Điều 11. Lễ đăng ký kết hôn tại Việt Nam 1. Trong thời hạn 05 ngày làm việc, kể từ ngày Chủ tịch Ủy ban nhân dân cấp tỉnh ký Giấy chứng nhận kết hôn, Sở Tư pháp thực hiện tổ chức lễ đăng ký kết hôn. 2. Lễ đăng ký kết hôn được tổ chức trang trọng tại trụ sở Sở Tư pháp. Khi tổ chức lễ đăng ký kết hôn phải có mặt hai bên nam, nữ. Đại diện Sở Tư pháp chủ trì hôn lễ, yêu cầu hai bên cho biết ý định lần cuối về sự tự nguyện kết hôn. Nếu hai bên đồng ý kết hôn thì đại diện Sở Tư pháp ghi việc kết hôn vào Sổ đăng ký kết hôn, yêu cầu từng bên ký tên vào Giấy chứng nhận kết hôn, Sổ đăng ký kết hôn và trao cho mỗi bên vợ, chồng 01 bản chính Giấy chứng nhận kết hôn. 3. Giấy chứng nhận kết hôn có giá trị kể từ ngày tổ chức lễ đăng ký kết hôn theo nghi thức quy định tại Khoản 2 Điều này. Việc cấp bản sao Giấy chứng nhận kết hôn từ sổ đăng ký kết hôn do Sở Tư pháp thực hiện theo yêu cầu của vợ, chồng. 4. Trường hợp có lý do chính đáng mà hai bên nam, nữ yêu cầu gia hạn thời gian tổ chức lễ đăng ký kết hôn quy định tại Khoản 1 Điều này thì được gia hạn ngày tổ chức lễ đăng ký kết hôn, nhưng không quá 90 ngày, kể từ ngày Chủ tịch Ủy ban nhân dân cấp tỉnh ký Giấy chứng nhận kết hôn. Hết thời hạn này mà hai bên nam, nữ không đến tổ chức lễ đăng ký kết hôn, Sở Tư pháp báo cáo Chủ tịch Ủy ban nhân dân cấp tỉnh; Giấy chứng nhận kết hôn được lưu trong hồ sơ. Trường hợp hai bên vẫn muốn kết hôn với nhau thì phải làm lại thủ tục đăng ký kết hôn từ đầu."
]

embeddings = model.encode(texts, normalize_embeddings=True)

sim_phaply = np.dot(embeddings[0], embeddings[1])
sim_khacbiet = np.dot(embeddings[0], embeddings[2])


print("Vector shape:", embeddings.shape)
print("Tương đồng giữa 2 câu cùng chủ đề 'Bộ Tư pháp':", sim_phaply)
print("Tương đồng giữa câu 'Bộ Tư pháp' và câu 'Lương lao động':", sim_khacbiet)
