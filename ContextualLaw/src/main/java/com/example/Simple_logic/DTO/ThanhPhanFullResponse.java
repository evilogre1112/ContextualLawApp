package com.example.Simple_logic.DTO;

import java.util.List;

public record ThanhPhanFullResponse(
    ArticleBriefResponse vanBan,
    List<ThanhPhanResponse> thanhPhanCha,
    ThanhPhanResponse thanhPhan,
    List<ThanhPhanResponse> thanhPhanCon
) {}