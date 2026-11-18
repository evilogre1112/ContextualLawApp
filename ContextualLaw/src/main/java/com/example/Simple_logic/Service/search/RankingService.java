package com.example.Simple_logic.Service.search;

import java.util.List;

import org.springframework.stereotype.Service;

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
}
