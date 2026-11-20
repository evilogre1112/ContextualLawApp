package com.example.Simple_logic.Service.search;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanFullResponse;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extension.Rranking;

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
    public List<ThanhPhanFullResponse> search(String query, int k) {
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
            switch (Rranking.chooseBranch(points.get(i))) {
                case THIS -> scores[i] = bm25Service.score(qTokens, candidate.getId());
                case PARENT -> {
                    ThanhPhan parent = scoringResult.parents().get(candidate.getId());
                    scores[i] = parent != null ? bm25Service.score(qTokens, parent.getId()) : 0.0;
                }
                case CHILD -> {
                    List<String> childIds = context.descendants().get(candidate.getId());
                    if (childIds != null) {
                        double maxChild = 0.0;
                        for (String childId : childIds) {
                            maxChild = Math.max(maxChild, bm25Service.score(qTokens, childId));
                        }
                        scores[i] = maxChild;
                    }
                }
                case NONE -> scores[i] = 0.0;
            }
        }
        return scores;
    }
}
