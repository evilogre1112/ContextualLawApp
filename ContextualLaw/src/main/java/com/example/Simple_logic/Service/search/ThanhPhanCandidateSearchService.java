package com.example.Simple_logic.Service.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.Config.SearchConfig;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;

@Service
public class ThanhPhanCandidateSearchService {

    private final ThanhPhanRepo thanhPhanRepo;
    private final CayThanhPhanCache cayThanhPhanCache;

    public ThanhPhanCandidateSearchService(
            ThanhPhanRepo thanhPhanRepo,
            CayThanhPhanCache cayThanhPhanCache) {
        this.thanhPhanRepo = thanhPhanRepo;
        this.cayThanhPhanCache = cayThanhPhanCache;
    }

    // Tìm ứng viên gần nhất và thu thập embedding của các thành phần con liên quan.
    public SearchContext findCandidates(double[] embeddingTimKiem, int k) {
        int soUngVien = Math.max(
                k * SearchConfig.CANDIDATE_MULTIPLIER,
                SearchConfig.MIN_CANDIDATE_COUNT);
        List<ThanhPhan> candidates =
                thanhPhanRepo.findEmbeddingGoc(
                        embeddingTimKiem,
                        soUngVien,
                        SearchConfig.VECTOR_SIMILARITY_THRESHOLD);

        Map<String, List<String>> descendants = cayThanhPhanCache.get();
        Set<String> childIds = new HashSet<>();
        for (ThanhPhan candidate : candidates) {
            List<String> children = descendants.get(candidate.getId());
            if (children != null) {
                childIds.addAll(children);
            }
        }

        Map<String, double[]> childEmbeddings = new HashMap<>();
        if (!childIds.isEmpty()) {
            for (ThanhPhan child : thanhPhanRepo.findAllById(childIds)) {
                if (child.getEmbedding() != null) {
                    childEmbeddings.put(child.getId(), child.getEmbedding());
                }
            }
        }

        return new SearchContext(candidates, descendants, childEmbeddings);
    }

    public record SearchContext(
            List<ThanhPhan> candidates,
            Map<String, List<String>> descendants,
            Map<String, double[]> childEmbeddings) {
    }
}
