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

            Double scoreParent = scores.x();
            Double scoreThis = scores.y();
            Double scoreChild = scores.z();
            ThanhPhan parent = scoringResult.parents().get(candidate.getId());
            ThanhPhan chosen = null;
            if ((scoreParent == null || scoreThis >= scoreParent) && (scoreChild == null || scoreThis >= scoreChild)) {
                chosen = ("Điểm".equals(candidate.getLoaithanhphan()) && parent != null) ? parent: candidate;
            } else if (scoreParent != null && (scoreChild == null || scoreParent >= scoreChild)) {
                chosen = parent;
            } else if (scoreChild != null) {
                chosen = candidate;
            }
            if (chosen != null && !"Chương".equals(chosen.getLoaithanhphan()) && !"Mục".equals(chosen.getLoaithanhphan())) {
                selected.putIfAbsent(chosen.getId(), chosen);
            }
        }
        if (selected.isEmpty()) {
            return List.of();
        }
        Set<String> vanBanIds = new HashSet<>();
        for (String tpId : selected.keySet()) {
            int slash = tpId.indexOf('/');
            if (slash > 0) {
                vanBanIds.add(tpId.substring(0, slash));
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
            int slash = tpId.indexOf('/');
            VanBan vanBan = slash > 0 ? vanBanById.get(tpId.substring(0, slash)) : null;
            List<String> parentIds = extractParentIdsFromRoot(tpId);
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

    /** Tách chuỗi id cha theo thứ tự từ cấp cao nhất xuống cha gần nhất. */
    private List<String> extractParentIdsFromRoot(String id) {
        List<String> reversed = new ArrayList<>();
        String cur = id;
        while (cur.contains("/")) {
            cur = cur.substring(0, cur.lastIndexOf('/'));
            if (cur.contains("/")) { // Bỏ id gốc của VanBan
                reversed.add(cur);
            }
        }
        List<String> ordered = new ArrayList<>(reversed.size());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            ordered.add(reversed.get(i));
        }
        return ordered;
    }
}
