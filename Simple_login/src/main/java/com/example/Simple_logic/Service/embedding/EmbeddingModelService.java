
package com.example.Simple_logic.Service.embedding;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.DTO.EmbeddingResponse;
import com.example.Simple_logic.Extension.Ttime;

@Service
public class EmbeddingModelService {

    private final RestClient client = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();

    private final Map<String, double[]> embeddingCache = new ConcurrentHashMap<>();

    // Tạo vector embedding cho một văn bản thông qua Python API.
    public double[] embed(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        long start = System.nanoTime();

        EmbeddingResponse response = client.post()
                .uri("http://127.0.0.1:8000/generate-embedding")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .body(EmbeddingResponse.class);

        Ttime.logTime("Tạo embedding", start);

        return response != null
                ? response.getEmbedding()
                : null;
    }

    // Tạo vector embedding cho danh sách văn bản thông qua Python API.
    public List<double[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        List<double[]> result = client.post()
                .uri("http://127.0.0.1:8000/embed/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("texts", texts))
                .retrieve()
                .body(new ParameterizedTypeReference<List<double[]>>() {});

        if (result == null || result.size() != texts.size()) {
            throw new IllegalStateException(
                    "Số lượng embedding trả về không khớp");
        }

        return result;
    }

    // Chỉ tạo embedding còn thiếu trong cache rồi trả về theo thứ tự văn bản đầu vào.
    public List<double[]> embedCoCache(List<String> texts) {

        List<String> ds = texts.stream()
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        List<String> chuaCoCache = ds.stream()
                .filter(s -> !embeddingCache.containsKey(s))
                .toList();

        if (!chuaCoCache.isEmpty()) {
            List<double[]> embeddings = embedBatch(chuaCoCache);

            for (int i = 0; i < chuaCoCache.size(); i++) {
                embeddingCache.put(
                        chuaCoCache.get(i),
                        embeddings.get(i));
            }
        }

        return ds.stream()
                .map(embeddingCache::get)
                .toList();
    }

    public Map<String, double[]> getEmbeddingCache() {
        return embeddingCache;
    }
}