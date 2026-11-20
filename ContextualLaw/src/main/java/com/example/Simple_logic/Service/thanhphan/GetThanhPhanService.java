package com.example.Simple_logic.Service.thanhphan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.EntityParentsResponse;
import com.example.Simple_logic.DTO.ThanhPhanFullResponse;
import com.example.Simple_logic.DTO.ThanhPhanResponse;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Extension.Ppath;
import com.example.Simple_logic.Mapper.ThanhPhanMapper;

@Service
public class GetThanhPhanService {
    private final VanBanRepo vanBanRepo;
    private final ThanhPhanRepo thanhPhanRepo;
    private final ThanhPhanMapper thanhPhanMapper;

    public GetThanhPhanService(VanBanRepo vanBanRepo, ThanhPhanRepo thanhPhanRepo,
            ThanhPhanMapper thanhPhanMapper) {
        this.vanBanRepo = vanBanRepo;
        this.thanhPhanRepo = thanhPhanRepo;
        this.thanhPhanMapper = thanhPhanMapper;
    }

    public ThanhPhanResponse getEntity(String id) {
        return thanhPhanMapper.toResponse(requireEntity(id));
    }

    private ThanhPhan requireEntity(String id) {
        return thanhPhanRepo.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy thành phần tương ứng"));
    }

    // Tải chuỗi thành phần cha từ ID phân cấp và trả về theo thứ tự từ gốc đến gần nhất.
    public EntityParentsResponse getEntityParents(String id) {
        String vanBanId = Ppath.extractVanBanId(id);
        if (vanBanId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID không hợp lệ hoặc không có thành phần cha");
        }
        List<String> parentIds = Ppath.extractParentIdsFromRoot(id);

        VanBan vanBan = vanBanRepo.findById(vanBanId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy văn bản tương ứng"));

        Map<String, ThanhPhan> thanhPhanMap = new HashMap<>();
        if (!parentIds.isEmpty()) {
            for (ThanhPhan thanhPhan : thanhPhanRepo.findAllById(parentIds)) {
                thanhPhanMap.put(thanhPhan.getId(), thanhPhan);
            }
        }

        List<ThanhPhan> parentEntities = new ArrayList<>(parentIds.size());
        for (String pId : parentIds) {
            ThanhPhan tp = thanhPhanMap.get(pId);
            if (tp == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy thành phần cha: " + pId);
            }
            parentEntities.add(tp);
        }

        return thanhPhanMapper.toParentsResponse(vanBan, parentEntities);
    }

    public List<ThanhPhanResponse> getEntityChilds(String id){
        requireEntity(id);
        return thanhPhanMapper.toResponses(thanhPhanRepo.getChild(id));
    } 

    // Ghép văn bản, chuỗi cha, thành phần hiện tại và các thành phần con trực tiếp.
    public ThanhPhanFullResponse getEntityFull(String id) {

        EntityParentsResponse parents = getEntityParents(id);

        ThanhPhanResponse current = thanhPhanMapper.toResponse(requireEntity(id));

        List<ThanhPhanResponse> children =
                thanhPhanMapper.toResponses(thanhPhanRepo.getChild(id));

        return thanhPhanMapper.toFullResponse(parents, current, children);
    }
}
