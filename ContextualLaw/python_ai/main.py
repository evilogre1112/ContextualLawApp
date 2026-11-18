from contextlib import asynccontextmanager
import logging
from fastapi import FastAPI
from pydantic import BaseModel
from embedding.model import get_embedding_model
from ner.api import router as ner_router, get_predictor
from document_processing.components import GetThanhPhan
from typing import Optional
from ranking.scoring import Rranking, Rranking2

logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    model = get_embedding_model()
    model.encode("khởi động", normalize_embeddings=True)

    predictor = get_predictor()
    predictor.predict("khởi động")
    yield

# Khởi tạo server FastAPI
app = FastAPI(lifespan=lifespan)
app.include_router(ner_router)


# Khai báo định dạng dữ liệu JSON mà Spring Boot sẽ gửi sang
class TextRequest(BaseModel):
    text: Optional[str] = None
    noiDung: Optional[str] = None 

# Tạo API nhận văn bản và trả về vector
@app.post("/generate-embedding")
def generate_embedding(request: TextRequest):
    # Nên thêm kiểm tra an toàn ở đây
    input_text = request.text or request.noiDung or ""
    if not input_text.strip():
        return {"embedding": []}
    vector = get_embedding_model().encode(input_text)
    return {
        "embedding": vector.tolist()
    }

@app.post("/Gettp")
def get_thanh_phan(request: TextRequest):
    input_text = request.text or request.noiDung or ""

    if not input_text.strip():
        return []

    return GetThanhPhan(input_text)

class ListPoint(BaseModel):
    ListPoints: list[list[float | None]]

@app.post("/Rranking")
def get_ranking(data: ListPoint):
    if(not data.ListPoints): return {"Ranking": []}
    return {"Ranking": Rranking(data.ListPoints)}

