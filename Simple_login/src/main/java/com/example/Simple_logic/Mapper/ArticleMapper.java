package com.example.Simple_logic.Mapper;

import org.mapstruct.Mapper;

import com.example.Simple_logic.DTO.ArticleRespone;
import com.example.Simple_logic.Entity.VanBan;

@Mapper(componentModel = "spring")
public interface ArticleMapper {
    ArticleRespone toResponse(VanBan vanBan);
}
