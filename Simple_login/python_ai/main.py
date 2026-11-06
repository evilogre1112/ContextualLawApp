from fastapi import FastAPI
from pydantic import BaseModel
from functools import lru_cache
from ner_xlmr import router as ner_router
from Gettp import GetThanhPhan

# Khởi tạo server FastAPI
app = FastAPI()
app.include_router(ner_router)

# Nạp mô hình AI (Lệnh này chỉ chạy 1 lần khi bật server, giúp tối ưu hiệu năng)
# Sử dụng mô hình 'all-MiniLM-L6-v2' (nhẹ, nhanh, chuẩn cho việc tạo embedding)
#http://127.0.0.1:8000/docs
@lru_cache(maxsize=1)
def get_embedding_model():
    from sentence_transformers import SentenceTransformer
    return SentenceTransformer('paraphrase-multilingual-mpnet-base-v2')

# Khai báo định dạng dữ liệu JSON mà Spring Boot sẽ gửi sang
class TextRequest(BaseModel):
    text: str

# Tạo API nhận văn bản và trả về vector
@app.post("/generate-embedding")
async def generate_embedding(request: TextRequest):
    # Đưa văn bản vào mô hình để tính toán ra vector
    vector = get_embedding_model().encode(request.text)
    # Chuyển đổi kết quả thành mảng (list) để có thể trả về dưới dạng JSON
    return {
        "embedding": vector.tolist()
    }
@app.post("/Gettp")
def get_thanh_phan(request: TextRequest):
    result = GetThanhPhan(request.text)
    return result 