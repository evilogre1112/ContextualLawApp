package com.example.Simple_logic.DTO;

import java.util.List;

public record ArticlePageResponse(
    List<ArticleResponse> articles,
    int trang,
    int soLuong,
    long tongSoBaiBao,
    int tongSoTrang
) {}
