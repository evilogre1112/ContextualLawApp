package com.example.Simple_logic.Service.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.example.Simple_logic.Config.SearchConfig;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;

import jakarta.annotation.PostConstruct;

import java.util.regex.Matcher;

import org.springframework.stereotype.Service;

@Service
public class Bm25Service {
    private final CayThanhPhanCache cayThanhPhanCache;
    private volatile Bm25Snapshot snapshot = new Bm25Snapshot(Map.of(), Map.of(), Map.of(), 1.0);
    
    public Bm25Service(CayThanhPhanCache cayThanhPhanCache) {
        this.cayThanhPhanCache = cayThanhPhanCache;
    }

    private record Bm25Snapshot(
            Map<String, Map<String, Integer>> tfById,
            Map<String, Integer> docLenById,
            Map<String, Double> idf,
            double avgdl) {
    }

    /** Tách văn bản thành danh sách token chữ thường (hỗ trợ tiếng Việt có dấu). */
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        Matcher matcher = SearchConfig.BM25_TOKEN_REGEX.matcher(text.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    /** Dựng lại toàn bộ chỉ mục BM25 trong RAM từ dữ liệu cache hiện tại. */
    @PostConstruct
    public void rebuild(){
        Collection<ThanhPhan> nodes = cayThanhPhanCache.getAllThanhPhan();
        if(nodes.isEmpty()){
            this.snapshot = new Bm25Snapshot(Map.of(), Map.of(), Map.of(), 1.0);
            return;
        }
        // Lưu tần suất xuất hiện của từng token trong từng văn bản
        Map<String, Map<String, Integer>> tfById = new HashMap<>(nodes.size());
        // Lưu số lượng token của từng văn bản
        Map<String, Integer> docLenById = new HashMap<>(nodes.size());
        // Lưu số lượng văn bản có chứa từng token
        Map<String, Integer> df = new HashMap<>();
        // Tổng số token của tất cả văn bản
        long totalLen = 0 ; 
        for (ThanhPhan tp : nodes){
            if(tp == null || tp.getId() == null) continue;
            // Tách nội dung thành danh sách token.
            List<String> tokens = tokenize(tp.getNoiDung());
            // Đếm số lần xuất hiện của từng token.
            Map<String, Integer> tf = new HashMap<>();
            // Đánh dấu token đã xuất hiện trong văn bản hiện tại.
            Set<String> seen = new HashSet<>();
            // Duyệt token, nếu chưa tồn tại thì gán 1 , tồn tại rồi thì cộng 1
            for (String token : tokens){
                tf.merge(token, 1, Integer::sum);
                if(seen.add(token)){
                    df.merge(token, 1, Integer::sum);
                }
            }
            tfById.put(tp.getId(),tf);
            docLenById.put(tp.getId(),tokens.size());
            totalLen += tokens.size();
        }
        int n = tfById.size();
        double avgdl = n > 0 ? Math.max(1.0, (double)totalLen/n):1.0;
        Map<String, Double> idf = new HashMap<>(df.size());
        for(Map.Entry<String,Integer> entry : df.entrySet()){
            int freq = entry.getValue();
            double val = Math.log((n-freq+0.5)/(freq + 0.5) + 1.0);
            idf.put(entry.getKey(),val);
        }
        this.snapshot = new Bm25Snapshot(tfById, docLenById, idf, avgdl) ;     

    }

     /** Tính điểm BM25 giữa truy vấn đã tách từ và một thành phần theo id. */
    public double score(List<String> queryTokens, String tpId) {
        if (queryTokens == null || queryTokens.isEmpty() || tpId == null) {
            return 0.0;
        }
        double K1 = SearchConfig.BM25_K1;
        double B = SearchConfig.BM25_B;
        Bm25Snapshot snap = this.snapshot;
        Map<String, Integer> docTf = snap.tfById().get(tpId);
        Integer dl = snap.docLenById().get(tpId);
        if (docTf == null || dl == null || dl == 0) {
            return 0.0;
        }
        double score = 0.0;
        for (String term : queryTokens) {
            Integer freq = docTf.get(term);
            if (freq == null || freq == 0) {
                continue;
            }
            double termIdf = snap.idf().getOrDefault(term, 0.0);
            double denom = freq + K1 * (1.0 - B + B * dl / snap.avgdl());
            score += termIdf * (freq * (K1 + 1.0)) / denom;
        }
        return score;
    }


}
