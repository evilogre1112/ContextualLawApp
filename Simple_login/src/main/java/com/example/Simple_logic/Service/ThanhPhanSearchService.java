package com.example.Simple_logic.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DTO.Point;
import com.example.Simple_logic.DTO.ThanhPhanRespone;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extention.*;


@Service
public class ThanhPhanSearchService {
  
    private final ThanhPhanRepo thanhPhanRepo;
    private final EmbeddingService embeddingService ;
    private final CayThanhPhanCache cayThanhPhanCache;
    private final RrankingService rrankingService;
    private final EmbeddingModelService embeddingModelService; 

    public ThanhPhanSearchService(
            ThanhPhanRepo thanhPhanRepo,
            EmbeddingService embeddingService,
            CayThanhPhanCache cayThanhPhanCache,
            RrankingService rrankingService,
            EmbeddingModelService embeddingModelService) {
        this.thanhPhanRepo = thanhPhanRepo;
        this.embeddingService = embeddingService;
        this.cayThanhPhanCache = cayThanhPhanCache;
        this.rrankingService = rrankingService;
        this.embeddingModelService = embeddingModelService;
    }

    private static final double NGUONG_SEARCH = 0.7;

   
    private ThanhPhanRespone toResponse(ThanhPhan tp) {
        ThanhPhanRespone res = new ThanhPhanRespone();

        res.setId(tp.getId());
        res.setLoai_thanh_phan(tp.getLoaithanhphan());
        res.setStt(tp.getStt());
        res.setNoi_dung(tp.getNoiDung());

        return res;
    }

    public List<ThanhPhanRespone> search(String query, int k) {
        double[] embeddingGoc = embeddingModelService.embed(query);

        if (embeddingGoc == null) {
            throw new IllegalStateException(
                    "Không tạo được embedding cho truy vấn");
        }

        double[] embeddingTimKiem = embeddingGoc;

        try {
            double[] vectorner = embeddingService.getEmbedNERInText(query);

            if (vectorner != null) {
                embeddingTimKiem = embeddingService.tronSerch(embeddingGoc, vectorner);
            }
        } catch (Exception e) {
            System.err.println(
                    "Bỏ qua NER của câu hỏi: " + e.getMessage());
        }
        int soUngVien = Math.max(k * 10, 100);

        List<ThanhPhan> dsEmbeddingGoc = thanhPhanRepo.findEmbeddingGoc(embeddingTimKiem, soUngVien, NGUONG_SEARCH);
        
        // Lấy tất cả id của thành phần con 
        Map<String, List<String>> dsCon = cayThanhPhanCache.get();
        Set<String> idCon = new HashSet<>();
        for (ThanhPhan tp : dsEmbeddingGoc) {
            List<String> c = dsCon.get(tp.getId());
            if (c != null) idCon.addAll(c);
        }

        Map<String, double[]> embCon = new HashMap<>();
        if (!idCon.isEmpty()) {
            for (ThanhPhan c : thanhPhanRepo.findAllById(idCon)) {
                if (c.getEmbedding() != null) {
                    embCon.put(c.getId(), c.getEmbedding());
                }
            }
        }

        List<Point> dsRank = new ArrayList<>();
        Map<String, ThanhPhan> parentCache = new HashMap<>();
        for (ThanhPhan tp : dsEmbeddingGoc) {
            double scoreThis = Vvec.cosineSimilarity(tp.getEmbeddingGoc(), embeddingTimKiem);
            ThanhPhan parent = thanhPhanRepo.getParent(tp.getId());
            parentCache.put(tp.getId(), parent);

            Double scoreParent = 0.0;
            if (parent != null && parent.getEmbedding() != null) {
                scoreParent = Vvec.cosineSimilarity(
                        parent.getEmbedding(), embeddingTimKiem);
            }
            double[] tBTPCon = embeddingService.getTrungBinhTPConEmbed(tp, dsCon, embCon);
          
            Double scoreChild = 0.0;
            if (tBTPCon != null) {
                embeddingService.merwithparent(tp,tBTPCon);
                tBTPCon = Vvec.ChuanHoaVec(tBTPCon); 
                scoreChild = Vvec.cosineSimilarity(tBTPCon, embeddingTimKiem);
            }
            Point u = new Point(scoreParent, scoreThis, scoreChild);
            dsRank.add(u);
        }

        List<Integer> R = rrankingService.Rranking(dsRank);
        Map<String, ThanhPhanRespone> kq = new LinkedHashMap<>();

        for (int j = 0; j < Math.min(k, R.size()); j++) {
            int index = R.get(j);

            ThanhPhan tp = dsEmbeddingGoc.get(index);
            Point scores = dsRank.get(index);

            Double scoreParent = scores.x();
            Double scoreThis = scores.y();
            Double scoreChild = scores.z();

            if ((scoreParent == null || scoreThis >= scoreParent)
                    && (scoreChild == null || scoreThis >= scoreChild)) {

                kq.putIfAbsent(tp.getId(), toResponse(tp));

            } else if (scoreParent != null
                    && (scoreChild == null || scoreParent >= scoreChild)) {

                ThanhPhan parent = parentCache.get(tp.getId());
                if (parent != null) {
                    kq.putIfAbsent(parent.getId(), toResponse(parent));
                }

            } else if (scoreChild != null) {

                List<String> childIds = dsCon.get(tp.getId());
                if (childIds != null && !childIds.isEmpty()) {
                    List<ThanhPhan> children = thanhPhanRepo.findAllById(childIds);
                    for (ThanhPhan child : children) {
                        kq.putIfAbsent(child.getId(), toResponse(child));
                    }
                }
            }
        }
        return new ArrayList<>(kq.values());
    }

}
