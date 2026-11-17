package com.example.Simple_logic.Service.search;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanRespone;

@Service
public class ThanhPhanSearchService {

    private final ThanhPhanQueryEmbeddingService queryEmbeddingService;
    private final ThanhPhanCandidateSearchService candidateSearchService;
    private final ThanhPhanSearchScoringService searchScoringService;
    private final RrankingService rrankingService;
    private final ThanhPhanSearchResultService searchResultService;

    public ThanhPhanSearchService(
            ThanhPhanQueryEmbeddingService queryEmbeddingService,
            ThanhPhanCandidateSearchService candidateSearchService,
            ThanhPhanSearchScoringService searchScoringService,
            RrankingService rrankingService,
            ThanhPhanSearchResultService searchResultService) {
        this.queryEmbeddingService = queryEmbeddingService;
        this.candidateSearchService = candidateSearchService;
        this.searchScoringService = searchScoringService;
        this.rrankingService = rrankingService;
        this.searchResultService = searchResultService;
    }

    public List<ThanhPhanRespone> search(String query, int k) {
        double[] embeddingTimKiem = queryEmbeddingService.createSearchEmbedding(query);
        ThanhPhanCandidateSearchService.SearchContext context =
                candidateSearchService.findCandidates(embeddingTimKiem, k);
        ThanhPhanSearchScoringService.ScoringResult scoringResult =
                searchScoringService.score(context, embeddingTimKiem);
        List<Point> rankingPoints = scoringResult.points();
        List<Integer> ranking = rrankingService.Rranking(rankingPoints);

        return searchResultService.buildResults(k, context, scoringResult, ranking);
    }
}
