package com.example.Simple_logic.Controller;

import com.example.Simple_logic.DTO.EntityParentsResponse;
import com.example.Simple_logic.DTO.ThanhPhanFullResponse;
import com.example.Simple_logic.DTO.ThanhPhanResponse;
import com.example.Simple_logic.Service.embedding.EmbeddingUpdateService;
import com.example.Simple_logic.Service.thanhphan.GetThanhPhanService;
import com.example.Simple_logic.Service.thanhphan.ThanhPhanCreationService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping ("/api/ArticleComponent")
public class ArticleComponentApi {
    private final EmbeddingUpdateService embeddingUpdateService;
    private final ThanhPhanCreationService thanhPhanCreationService;
    private final GetThanhPhanService getThanhPhanService;

    public ArticleComponentApi(
                                ThanhPhanCreationService thanhPhanCreationService,
                                EmbeddingUpdateService embeddingUpdateService,
                                GetThanhPhanService getThanhPhanService
) {
        this.thanhPhanCreationService = thanhPhanCreationService;
        this.embeddingUpdateService = embeddingUpdateService;
        this.getThanhPhanService = getThanhPhanService;
    }
    
    // Phân tích cấu trúc theo mã văn bản và trả về số thành phần được tạo.
    @PostMapping("/{id}/components")
    public ResponseEntity<?> createComponent(@PathVariable String id) {
        try {
            int total = thanhPhanCreationService.create(id);
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
    // Cập nhật embedding tổng hợp cho các thành phần đã lưu.
    @PostMapping("/merge-embedding")
    public ResponseEntity<?> mergeEmbedding() {
        try {
            embeddingUpdateService.capNhatEmbedding();
            
            return ResponseEntity.ok(
                Map.of("message", "Merge embedding thành công")
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                                .internalServerError()
                                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/re-embed")
    public ResponseEntity<?> reEmbed(){
        try {
            embeddingUpdateService.reEmbed();
            return ResponseEntity.ok(
                Map.of("Message","Tạo lại embedding thành công")
            );
        }catch (RuntimeException e){
            return  ResponseEntity
                                    .internalServerError()
                                    .body(Map.of("error",e.getMessage()));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<ThanhPhanResponse> getEntity(@RequestParam String id) {
        return ResponseEntity.ok(getThanhPhanService.getEntity(id));
    }

    @GetMapping("/getParents")
    public ResponseEntity<EntityParentsResponse> getEntityParents(@RequestParam String id) {
       return ResponseEntity.ok(getThanhPhanService.getEntityParents(id));
    }
    
    @GetMapping("/getChilds")
    public ResponseEntity<List<ThanhPhanResponse>> getEntityChilds(@RequestParam String id) {
        return ResponseEntity.ok(getThanhPhanService.getEntityChilds(id));
    }

    @GetMapping("/getFull")
    public ResponseEntity<ThanhPhanFullResponse> getEntityFull(@RequestParam String id) {

        return ResponseEntity.ok(
            getThanhPhanService.getEntityFull(id)
        );
    }
    
}
