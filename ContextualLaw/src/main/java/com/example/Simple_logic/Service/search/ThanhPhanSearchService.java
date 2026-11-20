package com.example.Simple_logic.Service.search;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanResponse;
import com.example.Simple_logic.Entity.ThanhPhan;

@Service
public class ThanhPhanSearchService {

    private final ThanhPhanQueryEmbeddingService queryEmbeddingService;
    private final ThanhPhanCandidateSearchService candidateSearchService;
    private final ThanhPhanSearchScoringService searchScoringService;
    private final RankingService rankingService;
    private final ThanhPhanSearchResultService searchResultService;
    private  final Bm25Service bm25Service;

    public ThanhPhanSearchService(
            ThanhPhanQueryEmbeddingService queryEmbeddingService,
            ThanhPhanCandidateSearchService candidateSearchService,
            ThanhPhanSearchScoringService searchScoringService,
            RankingService rankingService,
            ThanhPhanSearchResultService searchResultService,
            Bm25Service bm25Service) {
        this.queryEmbeddingService = queryEmbeddingService;
        this.candidateSearchService = candidateSearchService;
        this.searchScoringService = searchScoringService;
        this.rankingService = rankingService;
        this.searchResultService = searchResultService;
        this.bm25Service = bm25Service;
    }

    // Điều phối tạo embedding, tìm ứng viên, chấm điểm, xếp hạng và dựng kết quả.
    public List<ThanhPhanResponse> search(String query, int k) {
        double[] embeddingTimKiem = queryEmbeddingService.createSearchEmbedding(query);
        ThanhPhanCandidateSearchService.SearchContext context = candidateSearchService.findCandidates(embeddingTimKiem, k);
        ThanhPhanSearchScoringService.ScoringResult scoringResult = searchScoringService.score(context, embeddingTimKiem);
        List<Point> rankingPoints = scoringResult.points();
        double[] bm25Scores = computeBranchBm25Scores(query, context, scoringResult);
        List<Integer> ranking = rankingService.rankHybrid(rankingPoints, bm25Scores);

        return searchResultService.buildResults(k, context, scoringResult, ranking);
    }

    private double[] computeBranchBm25Scores(
        String query,
        ThanhPhanCandidateSearchService.SearchContext context,
        ThanhPhanSearchScoringService.ScoringResult scoringResult) 
    {
        List<ThanhPhan> candidates = context.candidates();
        List<Point> points = scoringResult.points();
        double[] scores = new double[candidates.size()];
        List<String> qTokens = bm25Service.tokenize(query);
        if (qTokens.isEmpty()) {
            return scores;
        }
        for (int i = 0; i < candidates.size(); i++) {
            ThanhPhan candidate = candidates.get(i);
            Point p = points.get(i);
            Double scoreParent = p.x();
            Double scoreThis = p.y();
            Double scoreChild = p.z();
            if ((scoreParent == null || scoreThis >= scoreParent)
                    && (scoreChild == null || scoreThis >= scoreChild)) {
                scores[i] = bm25Service.score(qTokens, candidate.getId());
            } else if (scoreParent != null && (scoreChild == null || scoreParent >= scoreChild)) {
                ThanhPhan parent = scoringResult.parents().get(candidate.getId());
                scores[i] = parent != null ? bm25Service.score(qTokens, parent.getId()) : 0.0;
            } else if (scoreChild != null) {
                List<String> childIds = context.descendants().get(candidate.getId());
                if (childIds != null) {
                    double maxChild = 0.0;
                    for (String childId : childIds) {
                        maxChild = Math.max(maxChild, bm25Service.score(qTokens, childId));
                    }
                    scores[i] = maxChild;
                }
            }
        }
        return scores;
    }
}
