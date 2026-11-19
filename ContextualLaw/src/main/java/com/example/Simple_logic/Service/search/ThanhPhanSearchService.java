package com.example.Simple_logic.Service.search;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanResponse;

@Service
public class ThanhPhanSearchService {

    private final ThanhPhanQueryEmbeddingService queryEmbeddingService;
    private final ThanhPhanCandidateSearchService candidateSearchService;
    private final ThanhPhanSearchScoringService searchScoringService;
    private final RankingService rankingService;
    private final ThanhPhanSearchResultService searchResultService;

    public ThanhPhanSearchService(
            ThanhPhanQueryEmbeddingService queryEmbeddingService,
            ThanhPhanCandidateSearchService candidateSearchService,
            ThanhPhanSearchScoringService searchScoringService,
            RankingService rankingService,
            ThanhPhanSearchResultService searchResultService) {
        this.queryEmbeddingService = queryEmbeddingService;
        this.candidateSearchService = candidateSearchService;
        this.searchScoringService = searchScoringService;
        this.rankingService = rankingService;
        this.searchResultService = searchResultService;
    }

    // Điều phối tạo embedding, tìm ứng viên, chấm điểm, xếp hạng và dựng kết quả.
    public List<ThanhPhanResponse> search(String query, int k) {
        double[] embeddingTimKiem = queryEmbeddingService.createSearchEmbedding(query);
        ThanhPhanCandidateSearchService.SearchContext context = candidateSearchService.findCandidates(embeddingTimKiem, k);
        ThanhPhanSearchScoringService.ScoringResult scoringResult = searchScoringService.score(context, embeddingTimKiem);
        List<Point> rankingPoints = scoringResult.points();
        List<Integer> ranking = rankingService.rank(rankingPoints);

        return searchResultService.buildResults(k, context, scoringResult, ranking);
    }
}
