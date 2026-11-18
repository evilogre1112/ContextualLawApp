package com.example.Simple_logic.Service.thanhphan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import com.example.Simple_logic.Mapper.ArticleMapper;
import com.example.Simple_logic.Mapper.ThanhPhanMapper;

@Service
public class GetThanhPhanService {
    private final VanBanRepo vanBanRepo;
    private final ArticleMapper articleMapper;
    private final ThanhPhanRepo thanhPhanRepo;
    private final ThanhPhanMapper thanhPhanMapper;

    public GetThanhPhanService(VanBanRepo vanBanRepo, ArticleMapper articleMapper, ThanhPhanRepo thanhPhanRepo,
            ThanhPhanMapper thanhPhanMapper) {
        this.vanBanRepo = vanBanRepo;
        this.articleMapper = articleMapper;
        this.thanhPhanRepo = thanhPhanRepo;
        this.thanhPhanMapper = thanhPhanMapper;
    }

    public ThanhPhanResponse getEntity(String id) {
        ThanhPhan result = thanhPhanRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy thành phần tương ứng"
        ));
        ThanhPhanResponse r = thanhPhanMapper.toResponse(result);
        return r;
    }

    public EntityParentsResponse getEntityParents(String id) {
        List<String> parentIds = new ArrayList<>();

        String parentId = id;

        while (parentId.contains("/")) {
            parentId = parentId.substring(0, parentId.lastIndexOf("/"));
            parentIds.add(parentId);
        }

        if (parentIds.isEmpty()) {
                                    throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "ID không hợp lệ hoặc không có thành phần cha"
                                    );
                                }
        String vanBanId = parentIds.remove(parentIds.size() - 1);
        
        VanBan vanBan = vanBanRepo.findById(vanBanId)
                                    .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Không tìm thấy văn bản tương ứng"
                                    ));
         Map<String, ThanhPhan> thanhPhanMap = StreamSupport.stream(
                                                                        thanhPhanRepo.findAllById(parentIds).spliterator(),
                                                                        false
                                                                    ).collect(Collectors.toMap(
                                                                        ThanhPhan::getId,
                                                                        Function.identity()
                                                                    ));
        List<ThanhPhanResponse> thanhPhanResponses = new ArrayList<>();

        for (int i = parentIds.size() - 1; i >= 0; i--) {
            ThanhPhan tp = thanhPhanMap.get(parentIds.get(i));

            if (tp == null) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy thành phần cha: " + parentIds.get(i)
                );
            }

            thanhPhanResponses.add(thanhPhanMapper.toResponse(tp));
        }                                                            

        
        return new EntityParentsResponse(
                                            articleMapper.toBriefResponse(vanBan),
                                            thanhPhanResponses
                                        );
    }

    public List<ThanhPhanResponse> getEntityChilds(String id){
        List<ThanhPhanResponse> resuList = new ArrayList<>();
        thanhPhanRepo.findById(id).orElseThrow(()-> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Không tìm thấy thành phần tương ứng"
        ));
        
        resuList = thanhPhanMapper.toResponses(thanhPhanRepo.getChild(id));
        return resuList;
    } 

    public ThanhPhanFullResponse getEntityFull(String id) {

        EntityParentsResponse parents = getEntityParents(id);

        ThanhPhanResponse current = getEntity(id);

        List<ThanhPhanResponse> children = getEntityChilds(id);

        return new ThanhPhanFullResponse(
            parents.vanBan(),
            parents.thanhPhanCha(),
            current,
            children
        );
    }
}
