package com.example.Simple_logic.Service.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanResponse;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Mapper.ThanhPhanMapper;
import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;

@Service
public class ThanhPhanSearchResultService {

    private final CayThanhPhanCache cayThanhPhanCache;
    private final ThanhPhanMapper thanhPhanMapper;

    public ThanhPhanSearchResultService(
            CayThanhPhanCache cayThanhPhanCache,
            ThanhPhanMapper thanhPhanMapper) {
        this.cayThanhPhanCache = cayThanhPhanCache;
        this.thanhPhanMapper = thanhPhanMapper;
    }

    // Chọn thành phần, cha hoặc các con phù hợp nhất dựa trên điểm xếp hạng.
    public List<ThanhPhanResponse> buildResults(
            int k,
            ThanhPhanCandidateSearchService.SearchContext context,
            ThanhPhanSearchScoringService.ScoringResult scoringResult,
            List<Integer> ranking) {
        Map<String, ThanhPhan> results = new LinkedHashMap<>();

        for (int j = 0; j < Math.min(k, ranking.size()); j++) {
            int index = ranking.get(j);
            ThanhPhan candidate = context.candidates().get(index);
            Point scores = scoringResult.points().get(index);

            Double scoreParent = scores.x();
            Double scoreThis = scores.y();
            Double scoreChild = scores.z();

            if ((scoreParent == null || scoreThis >= scoreParent)
                    && (scoreChild == null || scoreThis >= scoreChild)) {

                results.putIfAbsent(candidate.getId(), candidate);

            } else if (scoreParent != null
                    && (scoreChild == null || scoreParent >= scoreChild)) {

                ThanhPhan parent = scoringResult.parents().get(candidate.getId());
                if (parent != null) {
                    results.putIfAbsent(parent.getId(), parent);
                }

            } else if (scoreChild != null) {

                List<String> childIds = context.descendants().get(candidate.getId());
                if (childIds != null && !childIds.isEmpty()) {
                    List<ThanhPhan> children = cayThanhPhanCache.getThanhPhanByIds(childIds);
                    for (ThanhPhan child : children) {
                        results.putIfAbsent(child.getId(), child);
                    }
                }
            }
        }

        return thanhPhanMapper.toResponses(new ArrayList<>(results.values()));
    }
}
