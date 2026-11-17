package com.example.Simple_logic.Service.ner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.DAO.NerRepo;
import com.example.Simple_logic.DTO.NEREntityResponse;
import com.example.Simple_logic.DTO.NERResponse;
import com.example.Simple_logic.DTO.QuanHe;
import com.example.Simple_logic.Entity.NER;
import com.example.Simple_logic.Extension.*;

import jakarta.transaction.Transactional;

import org.springframework.http.MediaType;

@Service 
public class NERService {
    private final NerRepo nerRepo;
    private final RestClient client = RestClient.builder()
                                                .requestFactory(new SimpleClientHttpRequestFactory())
                                                .build();
    public NERService(NerRepo nerRepo) {
        this.nerRepo = nerRepo;
    }
    
    // Gửi văn bản đến Python API và nhận danh sách thực thể NER được trích xuất.
    public  List<NEREntityResponse> exNer(String text) {
        if (text == null || text.isBlank()) {
            return null;   
        }
        long start = System.nanoTime();
        NERResponse response = client.post()
                .uri("http://127.0.0.1:8000/extract-entities")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .body(new ParameterizedTypeReference<NERResponse>() {                    
                });
        Ttime.logTime("Lấy NER", start);
    if (response == null || response.entities() == null) {
        return List.of();
    }
        return response.entities() ;
    }

    // Trích xuất NER, chuẩn hóa tên và lưu hoặc tái sử dụng các node đã tồn tại.
    @Transactional 
    public  List<NER> saveNer(String text) {
        if (text == null || text.isBlank()) {
            return null;   
        }
        List<NEREntityResponse> response = exNer(text);
        long start = System.nanoTime();
        List<NER> entities = response.stream()
                                    .map(item -> nerRepo.add(new NER(
                                        null,
                                        item.label(),
                                        Normalize.normalizeName(item.text())
                                    ))).toList();
         Ttime.logTime("Lưu NER", start);
        return  entities;
    }

    // Gom tên NER theo mã thành phần từ các quan hệ đã lưu.
    public Map<String, List<String>> layTatCaNER() {

        List<QuanHe> rows = nerRepo.layTatCaNERThuocTP();

        Map<String, List<String>> ketQua = new HashMap<>();

        for (QuanHe row : rows) {
            ketQua.computeIfAbsent(
                    row.tpId(), k -> new ArrayList<>()).add(row.ten());
        }

        return ketQua;
    }
}
