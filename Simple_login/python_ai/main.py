from fastapi import FastAPI
from pydantic import BaseModel
from functools import lru_cache
from ner_xlmr import router as ner_router
from Gettp import GetThanhPhan
from typing import Optional
import traceback

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
    text: Optional[str] = None
    noiDung: Optional[str] = None 

# Tạo API nhận văn bản và trả về vector
@app.post("/generate-embedding")
async def generate_embedding(request: TextRequest):
    # Nên thêm kiểm tra an toàn ở đây
    input_text = request.text or request.noiDung or ""
    vector = get_embedding_model().encode(input_text)
    return {
        "embedding": vector.tolist()
    }

@app.post("/Gettp")
def get_thanh_phan(request: TextRequest):
    if(request.text):
        result = GetThanhPhan(request.text)
    else:
        result = GetThanhPhan(request.noiDung)
    return result 