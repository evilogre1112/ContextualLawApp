package com.example.Simple_logic.DTO;

import java.time.LocalDate;
import java.util.List;

import com.example.Simple_logic.Entity.VanBan_VanBan;
import com.example.Simple_logic.Entity.VanBan_ThanPhan;
import jakarta.validation.constraints.NotBlank;

public record CreateArticleRequest(
    String id,
    String noiDung,

    @NotBlank String tenVanBan,
    @NotBlank String url,

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
    String nguoiKy,

    List<VanBan_VanBan> vanBan_VanBans,
    List<VanBan_ThanPhan> vanBan_ThanPhans
) {}