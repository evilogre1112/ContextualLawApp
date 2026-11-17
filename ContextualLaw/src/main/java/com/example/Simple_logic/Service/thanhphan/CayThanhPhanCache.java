package com.example.Simple_logic.Service.thanhphan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DTO.QuanHe;

import jakarta.annotation.PostConstruct;

@Component
public class CayThanhPhanCache {
    private final ThanhPhanRepo thanhPhanRepo;
    private volatile Map<String, List<String>> danhSachCon = Map.of();

    public CayThanhPhanCache(ThanhPhanRepo repo) { this.thanhPhanRepo = repo; }

    // Tải lại ánh xạ thành phần cha-con từ kho dữ liệu vào bộ nhớ đệm.
    @PostConstruct
    public void reload() {
        Map<String, List<String>> m = new HashMap<>();
        for (QuanHe r : thanhPhanRepo.getDanhSachQuanHe())
            m.computeIfAbsent(r.tpId(), k -> new ArrayList<>()).add(r.ten());
        this.danhSachCon = m;   
    }
    public Map<String, List<String>> get() { return danhSachCon; }
}
