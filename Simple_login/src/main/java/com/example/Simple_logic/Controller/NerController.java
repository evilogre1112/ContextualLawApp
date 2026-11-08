package com.example.Simple_logic.Controller;

import com.example.Simple_logic.Entity.NER;
import com.example.Simple_logic.Service.NERService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Chuyển yêu cầu NER sang dịch vụ Python; không ghi dữ liệu vào Neo4j. */
@RestController
@RequestMapping("/api/ner")
public class NerController {
    NERService nerService;

    public NerController(NERService nerService) {
        this.nerService = nerService;
    }

    @PostMapping("/extract")
    public ResponseEntity<?> extract(@Valid @RequestBody String request) {
        List<NER> ner = nerService.saveNer(request);
       return ResponseEntity.ok(ner);
    }
}
