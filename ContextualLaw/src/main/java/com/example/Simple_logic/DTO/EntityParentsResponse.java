package com.example.Simple_logic.DTO;

import java.util.List;

public record EntityParentsResponse(
    ArticleBriefResponse vanBan,
    List<ThanhPhanResponse> thanhPhanCha
) {}
