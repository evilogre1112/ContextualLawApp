package com.example.Simple_logic.Service.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.EntityParentsResponse;
import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanFullResponse;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Extension.Ppath;
import com.example.Simple_logic.Extension.Rranking;
import com.example.Simple_logic.Mapper.ThanhPhanMapper;
import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;

@Service
public class ThanhPhanSearchResultService {

    private final CayThanhPhanCache cayThanhPhanCache;
    private final ThanhPhanMapper thanhPhanMapper;
    private final VanBanRepo vanBanRepo;

    public ThanhPhanSearchResultService(
            CayThanhPhanCache cayThanhPhanCache,
            ThanhPhanMapper thanhPhanMapper,
            VanBanRepo vanBanRepo) {
        this.cayThanhPhanCache = cayThanhPhanCache;
        this.thanhPhanMapper = thanhPhanMapper;
        this.vanBanRepo = vanBanRepo;
    }

    /** Chọn tối đa k thành phần đại diện rồi dựng đầy đủ văn bản, cha và con từ cache. */
    public List<ThanhPhanFullResponse> buildResults(
            int k,
            ThanhPhanCandidateSearchService.SearchContext context,
            ThanhPhanSearchScoringService.ScoringResult scoringResult,
            List<Integer> ranking) {
        Map<String, ThanhPhan> selected = new LinkedHashMap<>();

        for (int j = 0; j < Math.min(k, ranking.size()); j++) {
            int index = ranking.get(j);
            ThanhPhan candidate = context.candidates().get(index);
            Point scores = scoringResult.points().get(index);
            ThanhPhan parent = scoringResult.parents().get(candidate.getId());
            ThanhPhan chosen = switch (Rranking.chooseBranch(scores)) {
                case THIS -> ("Điểm".equals(candidate.getLoaithanhphan()) && parent != null)
                        ? parent
                        : candidate;
                case PARENT -> parent;
                case CHILD -> candidate;
                case NONE -> null;
            };
            if (chosen != null && !"Chương".equals(chosen.getLoaithanhphan()) && !"Mục".equals(chosen.getLoaithanhphan())) {
                selected.putIfAbsent(chosen.getId(), chosen);
            }
        }
        if (selected.isEmpty()) {
            return List.of();
        }
        Set<String> vanBanIds = new HashSet<>();
        for (String tpId : selected.keySet()) {
            String vanBanId = Ppath.extractVanBanId(tpId);
            if (vanBanId != null) {
                vanBanIds.add(vanBanId);
            }
        }
        Map<String, VanBan> vanBanById = new HashMap<>();
        if (!vanBanIds.isEmpty()) {
            for (VanBan vb : vanBanRepo.findAllById(vanBanIds)) {
                if (vb != null && vb.getId() != null) {
                    vanBanById.put(vb.getId(), vb);
                }
            }
        }
        List<ThanhPhanFullResponse> fullResults = new ArrayList<>(selected.size());
        for (ThanhPhan tp : selected.values()) {
            String tpId = tp.getId();
            String vanBanId = Ppath.extractVanBanId(tpId);
            VanBan vanBan = vanBanId != null ? vanBanById.get(vanBanId) : null;
            List<String> parentIds = Ppath.extractParentIdsFromRoot(tpId);
            List<ThanhPhan> parents = parentIds.isEmpty()
                    ? List.of()
                    : cayThanhPhanCache.getThanhPhanByIds(parentIds);
            List<String> childIds = context.descendants().get(tpId);
            List<ThanhPhan> children = (childIds == null || childIds.isEmpty())
                    ? List.of()
                    : cayThanhPhanCache.getThanhPhanByIds(childIds);
            EntityParentsResponse parentsDto = thanhPhanMapper.toParentsResponse(vanBan, parents);
            fullResults.add(thanhPhanMapper.toFullResponse(
                    parentsDto,
                    thanhPhanMapper.toResponse(tp),
                    thanhPhanMapper.toResponses(children)));
        }
        return fullResults;
    }
}
