package com.example.Simple_logic.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.Simple_logic.DTO.NEREntityResponse;
import com.example.Simple_logic.Entity.NER;

@Mapper(componentModel = "spring")
public interface NERMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loaiNER", source = "response.label")
    @Mapping(target = "tenNER", source = "normalizedName")
    NER toEntity(NEREntityResponse response, String normalizedName);
}
