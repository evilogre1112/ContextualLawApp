package com.example.Simple_logic.Service.thanhphan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DTO.QuanHe;
import com.example.Simple_logic.Entity.ThanhPhan;

import jakarta.annotation.PostConstruct;

@Component
public class CayThanhPhanCache {
    private final ThanhPhanRepo thanhPhanRepo;
    private volatile Map<String, List<String>> danhSachCon = Map.of();
    private volatile Map<String, String> chaCuaCon = Map.of();
    private final Map<String, ThanhPhan> thanhPhanById = new ConcurrentHashMap<>();

    public CayThanhPhanCache(ThanhPhanRepo repo) { this.thanhPhanRepo = repo; }

    // Tải lại ánh xạ thành phần cha-con và danh sách thành phần vào bộ nhớ đệm.
    @PostConstruct
    public void reload() {
        Map<String, List<String>> m = new HashMap<>();
        Map<String, String> p = new HashMap<>();
        for (QuanHe r : thanhPhanRepo.getDanhSachQuanHe()){
            m.computeIfAbsent(r.tpId(), k -> new ArrayList<>()).add(r.ten());
            p.putIfAbsent(r.ten(), r.tpId());
        }
        Map<String, ThanhPhan> loaded = new HashMap<>();
        for (ThanhPhan tp : thanhPhanRepo.getThanhPhanCoEmb()) {
            if (tp != null && tp.getId() != null) {
                loaded.put(tp.getId(), tp);
            }
        }
        this.danhSachCon = m;
        this.chaCuaCon = p;
        this.thanhPhanById.keySet().retainAll(loaded.keySet());
        this.thanhPhanById.putAll(loaded);
    }

    public Map<String, List<String>> get() { return danhSachCon; }

    public String getParentId(String childId) {
        return chaCuaCon.get(childId);
    }

    /** Lấy thành phần từ bộ nhớ đệm theo mã định danh. */
    public ThanhPhan getThanhPhan(String id) {
        return id == null ? null : thanhPhanById.get(id);
    }

    /** Trả về các thành phần theo đúng thứ tự mã đầu vào và nạp bổ sung các mã còn thiếu vào cache. */
    public List<ThanhPhan> getThanhPhanByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<String> missing = new LinkedHashSet<>();
        for (String id : ids) {
            if (id != null && !thanhPhanById.containsKey(id)) {
                missing.add(id);
            }
        }
        if (!missing.isEmpty()) {
            for (ThanhPhan tp : thanhPhanRepo.findAllFlatById(missing)) {
                if (tp != null && tp.getId() != null) {
                    thanhPhanById.put(tp.getId(), tp);
                }
            }
        }
        List<ThanhPhan> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            if (id == null) {
                continue;
            }
            ThanhPhan tp = thanhPhanById.get(id);
            if (tp != null) {
                result.add(tp);
            }
        }
        return result;
    }

    /** Trả về toàn bộ thành phần đang có trong cache. */
    public Collection<ThanhPhan> getAllThanhPhan() {
        return List.copyOf(thanhPhanById.values());
    }
}
