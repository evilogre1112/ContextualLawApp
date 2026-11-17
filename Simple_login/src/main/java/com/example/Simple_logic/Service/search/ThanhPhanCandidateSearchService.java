package com.example.Simple_logic.Service.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;

@Service
public class ThanhPhanCandidateSearchService {

    private static final double NGUONG_SEARCH = 0.7;

    private final ThanhPhanRepo thanhPhanRepo;
    private final CayThanhPhanCache cayThanhPhanCache;

    public ThanhPhanCandidateSearchService(
            ThanhPhanRepo thanhPhanRepo,
            CayThanhPhanCache cayThanhPhanCache) {
        this.thanhPhanRepo = thanhPhanRepo;
        this.cayThanhPhanCache = cayThanhPhanCache;
    }

    public SearchContext findCandidates(double[] embeddingTimKiem, int k) {
        int soUngVien = Math.max(k * 10, 100);
        List<ThanhPhan> candidates =
                thanhPhanRepo.findEmbeddingGoc(embeddingTimKiem, soUngVien, NGUONG_SEARCH);

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
