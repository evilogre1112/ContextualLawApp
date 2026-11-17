package com.example.Simple_logic.Service.search;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.Extension.Ttime;

@Service
public class RankingService {
    private final RestClient client = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();
    // Gửi điểm ứng viên đến Python API và nhận lại thứ tự xếp hạng.
    public List<Integer> rank(List<Point> p) {
        if (p == null || p.isEmpty()) {
            return List.of();
        }
        List<List<Double>> listPoints = p.stream().map(point -> Arrays.asList(point.x(), point.y(), point.z())).toList();
        long start = System.nanoTime();
        Map<String, Object> requestBody = Map.of(
                "ListPoints", listPoints);
        Map<String, List<Integer>> response = client.post()
                .uri("http://127.0.0.1:8000/Rranking")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, List<Integer>>>() {
                });
        Ttime.logTime("Xếp hạng", start);
        return response != null ? response.get("Ranking") : List.of();
    }
}
