data = [
        {
            "loai_thanh_phan" : tp.loai_thanh_phan,
            "stt": tp.stt,
            "ten": tp.ten,
            "noi_dung": tp.noi_dung
        }
        for tp in DanhSachThanhPhan
    ]

    json_data = json.dumps(data, ensure_ascii= False, indent = 4)
    print(json_data)