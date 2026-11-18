package com.example.Simple_logic.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.Simple_logic.DTO.CreateArticleRequest;
import com.example.Simple_logic.DTO.ArticleBriefResponse;
import com.example.Simple_logic.DTO.ArticleResponse;
import com.example.Simple_logic.Entity.VanBan;

@Mapper(componentModel = "spring")
public interface ArticleMapper {
    ArticleResponse toResponse(VanBan vanBan);

    @Mapping(target = "ten", source = "tenVanBan")
    ArticleBriefResponse toBriefResponse (VanBan vanBan);

    @Mapping(target = "vanBan_VanBans", defaultExpression = "java(java.util.List.of())")
    @Mapping(target = "vanBan_ThanPhans", defaultExpression = "java(java.util.List.of())")
    VanBan toEntity(CreateArticleRequest request);
}
