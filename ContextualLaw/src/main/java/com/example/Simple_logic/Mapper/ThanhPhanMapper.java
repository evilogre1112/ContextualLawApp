package com.example.Simple_logic.Mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.Simple_logic.DTO.ThanhPhanResponse;
import com.example.Simple_logic.Entity.ThanhPhan;

@Mapper(componentModel = "spring")
public interface ThanhPhanMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "stt", source = "stt")
    @Mapping(target = "loai_thanh_phan", source = "loaithanhphan")
    @Mapping(target = "noi_dung", source = "noiDung")
    ThanhPhanResponse toResponse(ThanhPhan thanhPhan);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "stt", source = "stt")
    @Mapping(target = "loai_thanh_phan", source = "loaithanhphan")
    @Mapping(target = "noi_dung", source = "noiDung")
    List<ThanhPhanResponse> toResponses(List<ThanhPhan> entities);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "stt", source = "stt")
    @Mapping(target = "loaithanhphan", source = "loai_thanh_phan")
    @Mapping(target = "noiDung", source = "noi_dung")
    void updateFromResponse(ThanhPhanResponse response, @MappingTarget ThanhPhan thanhPhan);
}
