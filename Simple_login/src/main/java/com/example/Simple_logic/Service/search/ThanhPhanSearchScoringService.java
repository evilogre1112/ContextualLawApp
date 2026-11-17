package com.example.Simple_logic.Service.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extention.Vvec;
import com.example.Simple_logic.Service.embedding.ThanhPhanVectorService;

@Service
public class ThanhPhanSearchScoringService {

    private final ThanhPhanRepo thanhPhanRepo;
    private final ThanhPhanVectorService thanhPhanVectorService;

    public ThanhPhanSearchScoringService(
            ThanhPhanRepo thanhPhanRepo,
            ThanhPhanVectorService thanhPhanVectorService) {
        this.thanhPhanRepo = thanhPhanRepo;
        this.thanhPhanVectorService = thanhPhanVectorService;
    }

    public ScoringResult score(
            ThanhPhanCandidateSearchService.SearchContext context,
            double[] embeddingTimKiem) {
        List<Point> points = new ArrayList<>();
        Map<String, ThanhPhan> parents = new HashMap<>();

        for (ThanhPhan candidate : context.candidates()) {
            double scoreThis = Vvec.cosineSimilarity(
                    candidate.getEmbeddingGoc(), embeddingTimKiem);
            ThanhPhan parent = thanhPhanRepo.getParent(candidate.getId());
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
