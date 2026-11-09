from functools import lru_cache
from typing import List, Union
from sentence_transformers import SentenceTransformer
from pyvi import ViTokenizer


class VietnameseBiEncoderWrapper:
    """Wrapper cho bkai-foundation-models/vietnamese-bi-encoder.
    Tự động phân đoạn từ (word segmentation) bằng pyvi trước khi mã hóa vector,
    đáp ứng đúng chuẩn đầu vào của PhoBERT backbone (768 chiều).
    """

    def __init__(self, model_name: str = "bkai-foundation-models/vietnamese-bi-encoder"):
        self.model = SentenceTransformer(model_name)

    def _segment(self, text: str) -> str:
        if not text:
            return ""
        return ViTokenizer.tokenize(text)

    def encode(self, texts: Union[str, List[str]], **kwargs):
        if isinstance(texts, str):
            segmented = self._segment(texts)
            return self.model.encode(segmented, **kwargs)
        else:
            segmented_list = [self._segment(t) for t in texts]
            return self.model.encode(segmented_list, **kwargs)


@lru_cache(maxsize=1)
def get_embedding_model():
    return VietnameseBiEncoderWrapper()
