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
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extension.*;
import com.example.Simple_logic.Mapper.NERMapper;

import jakarta.transaction.Transactional;

import org.springframework.http.MediaType;

@Service 
public class NERService {
    private final NerRepo nerRepo;
    private final NERMapper nerMapper;
    private final RestClient client = RestClient.builder()
                                                .requestFactory(new SimpleClientHttpRequestFactory())
                                                .build();
    public NERService(NerRepo nerRepo, NERMapper nerMapper) {
        this.nerRepo = nerRepo;
        this.nerMapper = nerMapper;
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
        List<NER> entities = new ArrayList<>();
        for (NEREntityResponse item : response) {
            String name = Normalize.chuanHoa(item.text());
            if (name.isBlank()) {
                continue;
            }
            entities.add(nerRepo.add(nerMapper.toEntity(item, name)));
        }
        Ttime.logTime("Lưu NER", start);
        return entities;
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

    @Transactional
    public void ganNerTheoLo(List<ThanhPhan> all) {
        if (all == null || all.isEmpty()) {
            return;
        }


        Map<String, NER> nerCache = new HashMap<>();
        for (NER existing : nerRepo.findAll()) {
            if (existing.getTenNER() != null && existing.getLoaiNER() != null) {
                nerCache.put(existing.getTenNER().trim() + "|" + existing.getLoaiNER().trim(), existing);
            }
        }

        List<NER> newNers = new ArrayList<>();
        int batchSize = 64;

        for (int i = 0; i < all.size(); i += batchSize) {
            int end = Math.min(i + batchSize, all.size());
            List<ThanhPhan> sub = all.subList(i, end);
            List<String> texts = sub.stream()
                    .map(tp -> tp.getNoiDung() == null ? "" : tp.getNoiDung())
                    .toList();

            List<NERResponse> responses = client.post()
                    .uri("http://127.0.0.1:8000/extract-entities/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("texts", texts))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<NERResponse>>() {});

            if (responses == null || responses.size() != sub.size()) {
                continue;
            }

            for (int j = 0; j < sub.size(); j++) {
                NERResponse res = responses.get(j);
                List<NER> tpNers = new ArrayList<>();
                if (res != null && res.entities() != null) {
                    for (NEREntityResponse item : res.entities()) {
                        String name = Normalize.chuanHoa(item.text());
                        String label = item.label() == null ? "" : item.label().trim();
                        if (name.isBlank() || label.isBlank()) {
                            continue;
                        }
                        String key = name + "|" + label;
                        NER ner = nerCache.get(key);
                        if (ner == null) {
                            ner = nerMapper.toEntity(item, name);
                            ner.setTenNER(name);
                            ner.setLoaiNER(label);
                            nerCache.put(key, ner);
                            newNers.add(ner);
                        }
                        tpNers.add(ner);
                    }
                }
                sub.get(j).setNers(tpNers);
            }
        }

        if (!newNers.isEmpty()) {
            nerRepo.saveAll(newNers);
        }
    }

    public List<NER> getNER(String id){
        if (id == null || id.isBlank()) return List.of();
        return nerRepo.getNERS(id);
    }
}
