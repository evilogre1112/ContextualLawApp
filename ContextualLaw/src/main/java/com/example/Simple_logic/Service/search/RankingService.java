package com.example.Simple_logic.Service.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.Config.SearchConfig;
import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.Extension.Rranking;
import com.example.Simple_logic.Extension.Ttime;

@Service
public class RankingService {

    // Xếp hạng trực tiếp bằng thuật toán Rranking trong Java Extension.
    public List<Integer> rank(List<Point> p) {
        if (p == null || p.isEmpty()) {
            return List.of();
        }
        long start = System.nanoTime();
        List<Integer> ranking = Rranking.rank(p);
        Ttime.logTime("Xếp hạng", start);
        return ranking;
    }

    public List<Integer> rankHybrid(List<Point> p, double[] bm25Scores){
        if (p == null || p.isEmpty()) {
            return List.of();
        }

        long start = System.nanoTime();
        double[] s3d = Rranking.score3D(p);

        double maxS3d = 0.0;
        double maxBm25 = 0.0;

         for (int i = 0; i < s3d.length; i++) {
            if (s3d[i] > maxS3d) {
                maxS3d = s3d[i];
            }
            if (bm25Scores != null && i < bm25Scores.length && bm25Scores[i] > maxBm25) {
                maxBm25 = bm25Scores[i];
            }
        }

        List<int[]> indexed = new ArrayList<>(s3d.length);
        double[] finalScores = new double[s3d.length];

        for (int i = 0; i < s3d.length; i++) {
            if (s3d[i] < 0.0) {
                continue;
            }
            double x = maxS3d > 0.0 ? s3d[i] / maxS3d : 0.0;
            double y = (bm25Scores != null && i < bm25Scores.length && maxBm25 > 0.0)
                    ? bm25Scores[i] / maxBm25
                    : 0.0;
            finalScores[i] = SearchConfig.HYBRID_RRANKING_WEIGHT * x
                    + SearchConfig.HYBRID_BM25_WEIGHT * y;
            indexed.add(new int[] { i });
        }
        indexed.sort((a, b) -> Double.compare(finalScores[b[0]], finalScores[a[0]]));
        List<Integer> ranking = new ArrayList<>(indexed.size());
        for (int[] item : indexed) {
            ranking.add(item[0]);
        }
        Ttime.logTime("Xếp hạng hybrid", start);
        return ranking;
    }
}
