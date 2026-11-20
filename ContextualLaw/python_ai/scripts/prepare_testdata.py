import argparse
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
PYTHON_AI_DIR = SCRIPT_DIR.parent
PROJECT_ROOT = PYTHON_AI_DIR.parent
DATADOC_DIR = PYTHON_AI_DIR / "datadoc"
TESTDATA_DIR = PROJECT_ROOT / "src" / "TestData"

SUPPORTED_EXTS = {".docx", ".doc", ".txt"}


def read_docx(path: Path) -> str:
    from docx import Document

    doc = Document(str(path))
    lines = [p.text.strip() for p in doc.paragraphs if p.text and p.text.strip()]
    return "\n".join(lines)


def read_doc_legacy(path: Path) -> str:
    soffice = shutil.which("soffice") or shutil.which("libreoffice")
    if soffice:
        with tempfile.TemporaryDirectory() as tmpdir:
            subprocess.run(
                [soffice, "--headless", "--convert-to", "docx", "--outdir", tmpdir, str(path)],
                check=True,
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
            )
            converted = Path(tmpdir) / f"{path.stem}.docx"
            if converted.is_file():
                return read_docx(converted)

    if sys.platform.startswith("win"):
        try:
            import win32com.client  # type: ignore

            word = win32com.client.Dispatch("Word.Application")
            word.Visible = False
            try:
                doc = word.Documents.Open(str(path.resolve()))
                text = doc.Content.Text
                doc.Close(False)
                lines = [line.strip() for line in text.replace("\r", "\n").splitlines() if line.strip()]
                return "\n".join(lines)
            finally:
                word.Quit()
        except Exception:
            pass

    raise RuntimeError(
        f"Không đọc trực tiếp được file .doc nhị phân ({path.name}). "
        "Hãy mở bằng Word/LibreOffice rồi lưu sang .docx hoặc cài pywin32."
    )


def extract_content_from_file(path: Path) -> str:
    ext = path.suffix.lower()
    if ext == ".docx":
        return read_docx(path)
    if ext == ".txt":
        return path.read_text(encoding="utf-8").strip()
    if ext == ".doc":
        return read_doc_legacy(path)
    raise ValueError(f"Định dạng chưa hỗ trợ: {ext} (hỗ trợ: .docx, .doc, .txt)")


def find_latest_doc(datadoc_dir: Path) -> Path:
    candidates = [
        p for p in datadoc_dir.iterdir()
        if p.is_file() and p.suffix.lower() in SUPPORTED_EXTS and not p.name.startswith("~$")
    ]
    if not candidates:
        raise FileNotFoundError(
            f"Thư mục '{datadoc_dir}' đang trống. "
            "Hãy chép file .docx / .doc / .txt vào đó hoặc truyền --text / đường dẫn file."
        )
    return max(candidates, key=lambda p: p.stat().st_mtime)


def next_vb_filename(testdata_dir: Path, custom_name: str | None = None) -> Path:
    testdata_dir.mkdir(parents=True, exist_ok=True)
    if custom_name:
        fname = custom_name if custom_name.endswith(".json") else f"{custom_name}.json"
        return testdata_dir / fname

    max_num = 0
    for p in testdata_dir.iterdir():
        m = re.fullmatch(r"VB(\d+)\.json", p.name, flags=re.IGNORECASE)
        if m:
            max_num = max(max_num, int(m.group(1)))
    return testdata_dir / f"VB{max_num + 1:02d}.json"


def build_template_payload(noi_dung: str) -> dict:
    return {
        "id": None,
        "noiDung": noi_dung,
        "tenVanBan": "",
        "url": "",
        "soHieu": "",
        "nganh": "",
        "linhVuc": "",
        "chucDanh": "",
        "tinhTrangHieuLuc": "",
        "loaiVanBan": "",
        "ngayBanHanh": None,
        "ngayCoHieuLuc": None,
        "ngayHetHieuLuc": None,
        "coQuanBanHanh": "",
        "nguoiKy": "",
        "vanBan_VanBans": [],
        "vanBan_ThanPhans": [],
    }


def main():
    DATADOC_DIR.mkdir(parents=True, exist_ok=True)
    TESTDATA_DIR.mkdir(parents=True, exist_ok=True)

    parser = argparse.ArgumentParser(
        description="Chuyển văn bản (.docx/.doc/.txt hoặc chuỗi) thành JSON mẫu trong TestData để điền metadata."
    )
    parser.add_argument(
        "input_file",
        nargs="?",
        help="Đường dẫn file đầu vào (nếu bỏ trống sẽ tự lấy file mới nhất trong python_ai/datadoc).",
    )
    parser.add_argument("--text", help="Truyền trực tiếp chuỗi nội dung văn bản.")
    parser.add_argument("--stdin", action="store_true", help="Đọc chuỗi nội dung văn bản từ stdin.")
    parser.add_argument("--name", help="Tên file JSON đầu ra (mặc định tự tăng VB04, VB05...).")
    args = parser.parse_args()

    if args.text is not None:
        noi_dung = args.text.strip()
        source_desc = "chuỗi --text"
    elif args.stdin:
        noi_dung = sys.stdin.read().strip()
        source_desc = "stdin"
    else:
        src_path = Path(args.input_file) if args.input_file else find_latest_doc(DATADOC_DIR)
        if not src_path.is_file():
            raise FileNotFoundError(f"Không tìm thấy file: {src_path}")
        noi_dung = extract_content_from_file(src_path)
        source_desc = str(src_path)

    if not noi_dung:
        raise ValueError("Nội dung văn bản trích ra bị rỗng.")

    out_path = next_vb_filename(TESTDATA_DIR, args.name)
    payload = build_template_payload(noi_dung)
    out_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")

    print(f"Nguồn   : {source_desc}")
    print(f"Độ dài  : {len(noi_dung)} ký tự ({noi_dung.count(chr(10)) + 1} dòng)")
    print(f"Đã lưu  : {out_path}")
    print("Hãy mở file trên để điền các trường còn trống (tenVanBan, url, soHieu, nganh, linhVuc...).")


if __name__ == "__main__":
    main()
