package com.example.Simple_logic.Service.search;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanRespone;
import com.example.Simple_logic.Entity.ThanhPhan;

@Service
public class ThanhPhanSearchResultService {

    private final ThanhPhanRepo thanhPhanRepo;

    public ThanhPhanSearchResultService(ThanhPhanRepo thanhPhanRepo) {
        this.thanhPhanRepo = thanhPhanRepo;
    }

    public List<ThanhPhanRespone> buildResults(
            int k,
            ThanhPhanCandidateSearchService.SearchContext context,
            ThanhPhanSearchScoringService.ScoringResult scoringResult,
            List<Integer> ranking) {
        Map<String, ThanhPhanRespone> results = new LinkedHashMap<>();

        for (int j = 0; j < Math.min(k, ranking.size()); j++) {
            int index = ranking.get(j);
            ThanhPhan candidate = context.candidates().get(index);
            Point scores = scoringResult.points().get(index);

            Double scoreParent = scores.x();
            Double scoreThis = scores.y();
            Double scoreChild = scores.z();

            if ((scoreParent == null || scoreThis >= scoreParent)
                    && (scoreChild == null || scoreThis >= scoreChild)) {

                results.putIfAbsent(candidate.getId(), toResponse(candidate));

            } else if (scoreParent != null
                    && (scoreChild == null || scoreParent >= scoreChild)) {

                ThanhPhan parent = scoringResult.parents().get(candidate.getId());
                if (parent != null) {
                    results.putIfAbsent(parent.getId(), toResponse(parent));
                }

            } else if (scoreChild != null) {

                List<String> childIds = context.descendants().get(candidate.getId());
                if (childIds != null && !childIds.isEmpty()) {
                    List<ThanhPhan> children = thanhPhanRepo.findAllById(childIds);
                    for (ThanhPhan child : children) {
                        results.putIfAbsent(child.getId(), toResponse(child));
                    }
                }
            }
        }

        return new ArrayList<>(results.values());
    }

    private ThanhPhanRespone toResponse(ThanhPhan thanhPhan) {
        ThanhPhanRespone response = new ThanhPhanRespone();

        response.setId(thanhPhan.getId());
        response.setLoai_thanh_phan(thanhPhan.getLoaithanhphan());
        response.setStt(thanhPhan.getStt());
        response.setNoi_dung(thanhPhan.getNoiDung());

        return response;
    }
}
