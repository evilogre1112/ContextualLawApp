package com.example.Simple_logic.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Chuyển yêu cầu NER sang dịch vụ Python; không ghi dữ liệu vào Neo4j. */
@RestController
@RequestMapping("/api/ner")
public class NerController {
    private final HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1)
        .build();
    private final ObjectMapper mapper;
    private final URI inferenceUri;

    public NerController(
            ObjectMapper mapper,
            @Value("${app.ner.url:http://127.0.0.1:8000/extract-entities}") String inferenceUrl) {
        this.mapper = mapper;
        this.inferenceUri = URI.create(inferenceUrl);
    }

    public record NerRequest(@NotBlank String text) {}

    @PostMapping("/extract")
    public ResponseEntity<?> extract(@Valid @RequestBody NerRequest request) {
        try {
            String body = mapper.writeValueAsString(Map.of("text", request.text()));
            HttpRequest upstream = HttpRequest.newBuilder(inferenceUri)
                    .timeout(Duration.ofSeconds(3000))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
            System.out.println("NER URL: " + inferenceUri);
            System.out.println("NER body: " + body);
            HttpResponse<String> response = client.send(
                    upstream, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200 || response.statusCode() == 422
                    || response.statusCode() == 503) {
                Object payload = mapper.readValue(response.body(), Object.class);
                return ResponseEntity.status(response.statusCode()).body(payload);
            }
            return ResponseEntity.status(502).body(Map.of(
                    "error", "Dịch vụ NER trả về HTTP " + response.statusCode()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(502).body(Map.of("error", "Yêu cầu NER bị gián đoạn"));
        } catch (IOException e) {
            return ResponseEntity.status(502).body(Map.of(
                    "error", "Không kết nối được dịch vụ NER: " + e.getMessage()));
        }
    }
}
