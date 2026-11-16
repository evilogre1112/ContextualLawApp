package com.example.Simple_logic.Controller;

import com.example.Simple_logic.Service.EmbeddingService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Simple_logic.Service.ThanhPhanService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;




@RestController 
@RequestMapping ("/api/ArticleComponent")
public class ArticleComponentApi {
    private final EmbeddingService embeddingService;
    private final ThanhPhanService thanhPhanService;

    public ArticleComponentApi(ThanhPhanService thanhPhanService,EmbeddingService embeddingService) {
        this.thanhPhanService = thanhPhanService;
        this.embeddingService = embeddingService;
    }
    
    @PostMapping("/{id}/components")
    public ResponseEntity<?> createComponent(@PathVariable String id) {
        try {
            int total = thanhPhanService.create(id);
            return ResponseEntity.ok(
                Map.of(
                    "message", "Phân tích cấu trúc văn bản thành công",
                    "totalComponents", total
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return ResponseEntity.internalServerError().body(Map.of("error", message));
        }
    }
    @PostMapping("/merge-embedding")
    public ResponseEntity<?> mergeEmbedding() {
        try {
            embeddingService.capNhatEmbedding();
            
            return ResponseEntity.ok(
                Map.of("message", "Merge embedding thành công")
            );

        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
}
