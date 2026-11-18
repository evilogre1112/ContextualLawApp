package com.example.Simple_logic.Mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.Simple_logic.DTO.EntityParentsResponse;
import com.example.Simple_logic.DTO.ThanhPhanFullResponse;
import com.example.Simple_logic.DTO.ThanhPhanResponse;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Entity.ThanhPhan;

@Mapper(componentModel = "spring", uses = ArticleMapper.class)
public interface ThanhPhanMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "stt", source = "stt")
    @Mapping(target = "loai_thanh_phan", source = "loaithanhphan")
    @Mapping(target = "noi_dung", source = "noiDung")
    ThanhPhanResponse toResponse(ThanhPhan thanhPhan);

    List<ThanhPhanResponse> toResponses(List<ThanhPhan> entities);

    @Mapping(target = "vanBan", source = "vanBan")
    @Mapping(target = "thanhPhanCha", source = "parentEntities")
    EntityParentsResponse toParentsResponse(VanBan vanBan, List<ThanhPhan> parentEntities);

    @Mapping(target = "vanBan", source = "parents.vanBan")
    @Mapping(target = "thanhPhanCha", source = "parents.thanhPhanCha")
    @Mapping(target = "thanhPhan", source = "current")
    @Mapping(target = "thanhPhanCon", source = "children")
    ThanhPhanFullResponse toFullResponse(
            EntityParentsResponse parents,
            ThanhPhanResponse current,
            List<ThanhPhanResponse> children);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "stt", source = "stt")
    @Mapping(target = "loaithanhphan", source = "loai_thanh_phan")
    @Mapping(target = "noiDung", source = "noi_dung")
    void updateFromResponse(ThanhPhanResponse response, @MappingTarget ThanhPhan thanhPhan);
}
