import json
import re 
from pathlib import Path
from datetime import datetime 

RAW_DIR = Path(r"D:\ContextualLawApp\AI_Train\vbpl_crawler\data\raw\documents")
CLEAN_DIR = Path(r"D:\ContextualLawApp\AI_Train\vbpl_crawler\data\documents")

def parse_date(date_str):
    if not date_str:
        return None 
    try:
        match = re .search(r'\d{2}/\d{2}/\d{4}',date_str)
        if match:
            return datetime.strptime(match.group(), '%d/%m/%Y').strftime('%Y-%m-%d')
    except Exception:
        pass
    return date_str

def map_attributes(attributes, result_dict):
    if not isinstance(attributes, list):
        return 
   
    #Danh sách các trường cần lấy
    target_keys = {
        "Số hiệu": "Số hiệu",
        "Ngành": "Ngành",
        "Lĩnh vực": "Lĩnh vực",
        "Loại văn bản" : "Loại văn bản",
        "Ngày ký xác thực" : "Ngày ký xác thực",
        "Ngày có hiệu lực" : "Ngày có hiệu lực",
        "Cơ quan ban hành" : "Cơ quan ban hành",
        "Người ký" : "Người ký"
    }

    for attr in attributes:
        label = attr.get("label" or "").strip()
        value = attr.get("value" or "").strip()
        for key, output_key in target_keys.items():
            if key in label :
                if "Ngày" in key:
                    result_dict[output_key] = parse_date(value)
                else :
                    result_dict[output_key] = value 
                break 

def process_file(file_path):
    try: 
        data = json.loads(file_path.read_text(encoding = "utf-8"))
        record = {
            "id": "",
            "Nội dung": "",
            "Tên văn bản": "",
            "URL": "",
            "Số hiệu": None,
            "Ngành": None,
            "Lĩnh vực": None,
            "Chức danh": None,
            "Loại văn bản": None,
            "Ngày ký xác thực": None,
            "Ngày có hiệu lực": None,
            "Cơ quan ban hành": None,
            "Người ký": None
        }
        record["id"] = "vb:" + str(data.get("id","") or data.get("document_id","")) 
        record["Nội dung"] = data.get("Nội dung","") or data.get("full_text","")
        record["Tên văn bản"] = data.get("title","") or data.get("Tên văn bản","")
        record["URL"] = data.get("URL") or data.get("source_url","")

        if not record["Tên văn bản"] or not record["URL"]:
            print(f"Skip: {file_path} --> URL AND Title null")
            return None 
        if "attributes" or "Thuộc tính gốc" in data:
            map_attributes(data["attributes"],record)
        else:
           print(f"Skip: {file_path.name} --> Missing or invalid attributes")
           return None 
        return record
    except Exception as e:
        print (f"[!] Error : can not open {file_path.name}: {e}")
        return None 
                

def main():
    CLEAN_DIR.mkdir(parents = True, exist_ok = True)

    processed_count = 0 
    skipped_count = 0 

    for json_file in RAW_DIR.glob("*.json"):
        record = process_file(json_file)

        if record:
            filename = record["id"].replace(":", "_") + ".json"
            output_path = CLEAN_DIR / filename
            output_path.write_text(
                json.dumps(record, ensure_ascii=False, indent=4),
                encoding= "utf-8"
            )
            processed_count += 1 
        else :
            skipped_count += 1
    print("-" * 30)
    print(f"Completed: {processed_count} files")
    print(f"Skipped: {skipped_count} files")
    print(f"Save in: {CLEAN_DIR}")

if __name__ == "__main__":
    main()
 