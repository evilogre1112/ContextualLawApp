package com.example.Simple_logic.DTO;

import java.time.LocalDate;

public record ArticleRespone(
    String id,
    String noiDung,
    String tenVanBan,
    String url,
    String soHieu,
    String nganh,
    String linhVuc,
    String chucDanh,
    String loaiVanBan,
    String tinhTrangHieuLuc,
    LocalDate ngayBanHanh,
    LocalDate ngayCoHieuLuc,
    LocalDate ngayHetHieuLuc,
    String coQuanBanHanh,
    String nguoiKy
) {}
