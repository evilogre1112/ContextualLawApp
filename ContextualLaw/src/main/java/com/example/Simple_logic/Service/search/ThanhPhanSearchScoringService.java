package com.example.Simple_logic.Service.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extension.Vvec;
import com.example.Simple_logic.Service.embedding.ThanhPhanVectorService;
import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;

@Service
public class ThanhPhanSearchScoringService {

    private final ThanhPhanVectorService thanhPhanVectorService;
    private final CayThanhPhanCache cayThanhPhanCache;

    public ThanhPhanSearchScoringService(
            ThanhPhanVectorService thanhPhanVectorService,
            CayThanhPhanCache cayThanhPhanCache) {
        this.thanhPhanVectorService = thanhPhanVectorService;
        this.cayThanhPhanCache = cayThanhPhanCache ;
    }

    // Tính điểm tương đồng của từng ứng viên, thành phần cha và các thành phần con.
    public ScoringResult score(
            ThanhPhanCandidateSearchService.SearchContext context,
            double[] embeddingTimKiem) {
        List<Point> points = new ArrayList<>();
        Map<String, ThanhPhan> parents = new HashMap<>();
        Set<String> parentIds = new HashSet<>();
        for (ThanhPhan candidate : context.candidates()) {
            String parentId = cayThanhPhanCache.getParentId(candidate.getId());
            if (parentId != null) {
                parentIds.add(parentId);
            }
        }
        Map<String, ThanhPhan> parentById = new HashMap<>();
        if (!parentIds.isEmpty()) {
            for (ThanhPhan p : cayThanhPhanCache.getThanhPhanByIds(parentIds)) {
                parentById.put(p.getId(), p);
            }
        }       
        for (ThanhPhan candidate : context.candidates()) {
            double scoreThis = Vvec.cosineSimilarity(candidate.getEmbeddingGoc(), embeddingTimKiem);
            String parentId = cayThanhPhanCache.getParentId(candidate.getId());
            ThanhPhan parent = parentId != null ? parentById.get(parentId) : null;
            parents.put(candidate.getId(), parent);

            Double scoreParent = 0.0;
            if (parent != null && parent.getEmbedding() != null) {
                scoreParent = Vvec.cosineSimilarity(
                        parent.getEmbedding(), embeddingTimKiem);
            }

            double[] childEmbedding = thanhPhanVectorService.averageChildEmbeddings(
                    candidate, context.descendants(), context.childEmbeddings());

            Double scoreChild = 0.0;
            if (childEmbedding != null) {
                thanhPhanVectorService.mergeWithParent(candidate, childEmbedding);
                childEmbedding = Vvec.ChuanHoaVec(childEmbedding);
                scoreChild = Vvec.cosineSimilarity(childEmbedding, embeddingTimKiem);
            }

            points.add(new Point(scoreParent, scoreThis, scoreChild));
        }

        return new ScoringResult(points, parents);
    }

    public record ScoringResult(
            List<Point> points,
            Map<String, ThanhPhan> parents) {
    }
}
