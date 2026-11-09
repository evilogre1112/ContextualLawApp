"""Thin FastAPI adapter around the ORIGINAL trained project's ViTagger.

The client sends a raw text string. The training project's own ViTagger handles
its normalization, segmentation, tokenization, word alignment and CRF decoding.
We intentionally DO NOT reimplement any of those steps here.
"""

import importlib
import logging
import os
from pydantic import BaseModel
import sys
from pathlib import Path
from threading import Lock
from embedding_model import get_embedding_model
from typing import List

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

logger = logging.getLogger(__name__)
router = APIRouter()

# python_ai/ner_xlmr.py lives at ContextualLawApp/Simple_login/python_ai/.
DEFAULT_TRAIN_ROOT = (
    Path(__file__).resolve().parents[2] / "AI_Train" / "PhoBERT-Legal-NER-main(1)"
)
_predictor = None
_load_lock = Lock()


class NerSetupError(RuntimeError):
    """Native inference service cannot be loaded."""


class NerRequest(BaseModel):
    text: str = Field(min_length=1)


class BatchEmbeddingRequest(BaseModel):
    texts: List[str]

def _paths():
    root = Path(os.environ.get("LEGAL_NER_TRAIN_ROOT") or DEFAULT_TRAIN_ROOT).expanduser().resolve()
    checkpoint = Path(os.environ.get("LEGAL_NER_CHECKPOINT") or root / "outputs" / "xlmr_original_b16" / "best_model.pt").expanduser().resolve()
    jar = Path(os.environ.get("VNCORENLP_JAR") or root / "vncorenlp" / "VnCoreNLP-1.1.1.jar").expanduser().resolve()
    return root, checkpoint, jar


def _native_class(root: Path):
    """Import the user's installed training-side predictor, not a duplicated model class."""
    original = root / "vphoberttagger" / "predictor.py"
    if not original.is_file():
        raise NerSetupError(
            f"Không tìm thấy predictor.py của chương trình gốc: {original}. "
            "Cần thư mục mã nguồn huấn luyện đầy đủ, không chỉ best_model.pt."
        )
    if str(root) not in sys.path:
        sys.path.insert(0, str(root))
    try:
        module = importlib.import_module("vphoberttagger.predictor")
    except Exception as exc:
        raise NerSetupError(
            f"Không import được predictor gốc ({original}): {exc}. "
            "Dùng cùng môi trường Python với chương trình gọi model trực tiếp."
        ) from exc
    imported = Path(module.__file__).resolve()
    if imported != original.resolve():
        raise NerSetupError(
            f"Import nhầm predictor từ {imported}; cần {original}. "
            "Hãy dùng process Python sạch rồi khởi động lại uvicorn."
        )
    vi_tagger = getattr(module, "ViTagger", None)
    if vi_tagger is None:
        raise NerSetupError(f"predictor gốc không có class ViTagger: {original}")
    return vi_tagger


class NativePredictor:
    """Use ViTagger's ORIGINAL predict code with absolute external model paths.

    The native ViTagger.__init__ uses a hard-coded relative VnCoreNLP JAR path.
    Override just its one-time constructor so the whole server's cwd doesn't
    have to change. All preprocessing/convert_tensor/__call__ methods are native.
    """

    def __init__(self, root: Path, checkpoint: Path, jar: Path):
        if not checkpoint.is_file():
            raise NerSetupError(f"Không có checkpoint: {checkpoint}")
        if not jar.is_file():
            raise NerSetupError(f"Không có VnCoreNLP JAR: {jar}")
        if not (jar.parent / "models" / "wordsegmenter").is_dir():
            raise NerSetupError(f"Không có models/wordsegmenter: {jar.parent}")

        OriginalViTagger = _native_class(root)
        try:
            # On some Windows/JDK combinations Jetty (inside VnCoreNLP) cannot
            # create its temporary Unix-domain socket in the default user temp
            # path. Use the short Windows temp path before Java is launched.
            if os.name == "nt":
                socket_tmp = Path(os.environ.get("SystemRoot", r"C:\Windows")) / "Temp"
                socket_tmp.mkdir(parents=True, exist_ok=True)
                probe = socket_tmp / f"ner-java-temp-{os.getpid()}.tmp"
                probe.touch()
                probe.unlink()

                os.environ["TEMP"] = str(socket_tmp)
                os.environ["TMP"] = str(socket_tmp)
                # Java/Jetty uses this property for its Unix-domain socket;
                # TEMP/TMP alone did not fix the VnCoreNLP startup failure.
                java_options = os.environ.get("JAVA_TOOL_OPTIONS", "").strip()
                socket_option = f"-Djdk.net.unixdomain.tmpdir={socket_tmp}"
                os.environ["JAVA_TOOL_OPTIONS"] = f"{java_options} {socket_option}".strip()
                logger.info("VnCoreNLP Java socket temp directory: %s", socket_tmp)

            import torch
            from vncorenlp import VnCoreNLP

            class AbsolutePathViTagger(OriginalViTagger):
                def __init__(self, path, jar_path):
                    #Chọn gpu phù hợp
                    if torch.cuda.is_available():
                        self.device = "cuda"
                    elif hasattr(torch, "xpu") and torch.xpu.is_available():
                        self.device = "xpu"
                    elif hasattr(torch.backends, "mps") and torch.backends.mps.is_available():
                        self.device = "mps"
                    else:
                        self.device = "cpu"

                    logger.info("NER đang chạy trên thiết bị: %s", self.device)
                    self.rdrsegmenter = VnCoreNLP(
                        str(jar_path), annotators="wseg", max_heap_size="-Xmx500m"
                    )
                    (self.model, self.tokenizer, self.max_seq_len,
                     self.label2id, self.use_crf) = self.load_model(
                        str(path), device=self.device
                    )
                    self.id2label = {idx: label for idx, label in enumerate(self.label2id)}

            self.engine = AbsolutePathViTagger(checkpoint, jar)
        except Exception as exc:
            raise NerSetupError(f"Không khởi tạo được ViTagger gốc: {exc}") from exc
        self.lock = Lock()
        logger.info("Original ViTagger loaded once from %s", root)

    def predict(self, raw_text: str):
        # CRITICAL: pass the complete original user input to ViTagger.__call__.
        # ViTagger handles Vietnamese segmentation and BIO decoding internally.
        with self.lock:
            import torch
            if self.engine.device == "xpu":
                free, total = torch.xpu.memory.mem_get_info()
                allocated = torch.xpu.memory.memory_allocated()
                reserved = torch.xpu.memory.memory_reserved()

                logger.info(
                    "XPU: free=%.2f/%.2f GB, allocated=%.2f GB, reserved=%.2f GB",
                    free / 1024**3,
                    total / 1024**3,
                    allocated / 1024**3,
                    reserved / 1024**3,
                )
            native_entities = self.engine(raw_text)
        entities = []
        for entity in native_entities:
            if not isinstance(entity, (tuple, list)) or len(entity) != 2:
                raise RuntimeError(
                    "Đầu ra ViTagger gốc không phải các cặp (văn bản, nhãn). "
                    "Kiểm tra lại predictor.py đang dùng."
                )
            value, label = entity
            entities.append({"text": str(value), "label": str(label)})
        return {"model": "XLM-R+CRF", "entities": entities}


def get_predictor():
    global _predictor
    if _predictor is None:
        with _load_lock:
            if _predictor is None:
                root, checkpoint, jar = _paths()
                _predictor = NativePredictor(root, checkpoint, jar)
    return _predictor


@router.get("/ner/status")
def ner_status():
    root, checkpoint, jar = _paths()
    return {
        "ready": _predictor is not None,
        "checkpoint_found": checkpoint.is_file(),
        "vncorenlp_jar_found": jar.is_file(),
        "wordsegmenter_found": (jar.parent / "models" / "wordsegmenter").is_dir(),
        "original_predictor_found": (root / "vphoberttagger" / "predictor.py").is_file(),
    }


@router.post("/extract-entities")
def extract_entities(request: NerRequest):
    if not request.text.strip():
        raise HTTPException(status_code=422, detail="text không được để trống")
    try:
        return get_predictor().predict(request.text)
    except NerSetupError as exc:
        logger.exception("Không thể khởi tạo native NER predictor")
        raise HTTPException(status_code=503, detail=str(exc)) from exc

@router.post("/embed/batch")
def embed_batch(request: BatchEmbeddingRequest):
    if not request.texts:
        return []
    try:
        model = get_embedding_model()
        embeddings = model.encode(
            request.texts,
            batch_size = 32,
            convert_to_numpy = True,
            normalize_embeddings = True 
        )
        return embeddings.tolist()
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Không thể tạo batch embedding: {str(e)}"
        )