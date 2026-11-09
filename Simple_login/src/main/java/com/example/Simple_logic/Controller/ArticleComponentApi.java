package com.example.Simple_logic.Controller;

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
    private final ThanhPhanService thanhPhanService;

    public ArticleComponentApi(ThanhPhanService thanhPhanService) {
        this.thanhPhanService = thanhPhanService;
    }
    
    @PostMapping("/{id}/components")
    public ResponseEntity<?> createComponet(@PathVariable String id) {
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
            thanhPhanService.merEmndeThanhPhan();

            return ResponseEntity.ok(
                Map.of(
                    "message", "Trộn embedding thành phần và NER thành công"
                )
            );

        } catch (RuntimeException e) {
            String message = e.getMessage() == null
                ? e.getClass().getSimpleName()
                : e.getMessage();

            return ResponseEntity.internalServerError()
                .body(Map.of("error", message));
        }
    }
}
