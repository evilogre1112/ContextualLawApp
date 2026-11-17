import json
from pathlib import Path

payload = {
    "noiDung": Path("noiDung.txt").read_text(encoding="utf-8"),
}

Path("VB02.json").write_text(
    json.dumps(payload, ensure_ascii=False, indent=2),
    encoding="utf-8"
)