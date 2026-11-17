package com.example.Simple_logic.Service;



import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;


import com.example.Simple_logic.Config.EmbeddingConfig;
import com.example.Simple_logic.DTO.NEREntityResponse;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extention.Vvec;

@Service
public class EmbeddingService {
    private final NERService nerService;
    private  final EmbeddingModelService embeddingModelService;
   

    EmbeddingService(NERService nerService,
                     EmbeddingModelService embeddingModelService
    ) {
        this.nerService = nerService ;
        this.embeddingModelService = embeddingModelService;
    }


    public void merwithparent(ThanhPhan parent, double[] embedtpcon) {
        if (parent == null || parent.getEmbeddingGoc() == null) return;
        cong(embedtpcon, parent.getEmbeddingGoc(), EmbeddingConfig.HE_SO_CHA);
    }
    
    /** v += w * (u / |u|) */
    public void cong(double[] v, double[] u, double w) {
        if (u.length != v.length) {
            throw new IllegalStateException(
                    "Số chiều lệch nhau: " + u.length + " và " + v.length);
        }

        double norm = 0;
        for (double x : u)
            norm += x * x;
        norm = Math.sqrt(norm);

        if (norm == 0) {
            throw new IllegalStateException("Vector có độ dài 0");
        }

        for (int i = 0; i < v.length; i++) {
            v[i] += w * u[i] / norm;
        }
    }

    public double[] getTrungBinhTPConEmbed(ThanhPhan tp, Map<String, List<String>> danhSachCon,Map<String, double[]> nguonEmb) {
        List<String> dsIdCon = danhSachCon.get(tp.getId());

        if (dsIdCon == null || dsIdCon.isEmpty()) {
            return null;
        }
        double[] tong = null;

        for (String idCon : dsIdCon) {
            double[] emb = nguonEmb.get(idCon);
            if (emb == null) continue;
            if (tong == null) tong = new double[emb.length];
            cong(tong, emb, 1.0);
        }

        return tong == null ? null : Vvec.ChuanHoaVec(tong);
    }

    private List<double[]> embedCoCache(List<String> tenNers) {
        List<String> chuaCoCache = tenNers.stream()
                .filter(ten -> !embeddingModelService.getEmbeddingCache().containsKey(ten))
                .distinct()
                .toList();
        if (!chuaCoCache.isEmpty()) {
            List<double[]> embeddings = embeddingModelService.embedBatch(chuaCoCache);
            if (embeddings == null || embeddings.size() != chuaCoCache.size()) {
                throw new IllegalStateException("Số lượng embedding trả về không khớp số lượng NER");
            }
            for (int i = 0; i < chuaCoCache.size(); i++) {
                embeddingModelService.getEmbeddingCache().put(chuaCoCache.get(i), embeddings.get(i));
            }
        }
        return tenNers.stream()
                .map(embeddingModelService.getEmbeddingCache()::get)
                .toList();
    }

    public double[] trungBinhNer(List<String> tenNER) {
        List<String> ds = tenNER.stream()
                .filter(s -> s != null && !s.isBlank())
                .toList();
        if (ds.isEmpty()) {
            return null;
        }
        List<double[]> dsEmbedding = embedCoCache(ds);

        double[] tb = new double[768];
        for (int j = 0; j < dsEmbedding.size(); j++) {
            double[] e = dsEmbedding.get(j);
            String ten = ds.get(j);
            double n = 0;
            for (double x : e)
                n += x * x;
            n = Math.sqrt(n);
            if (n == 0) {
                throw new IllegalStateException("Vector NER có độ dài 0: " + ten);
            }
            for (int i = 0; i < 768; i++) {
                tb[i] += e[i] / n / ds.size();
            }
        }

        return Vvec.ChuanHoaVec(tb);
    }
    

    public double[] getEmbedNERInText(String text) {
        List<NEREntityResponse> ds = nerService.exNer(text);
        if (ds == null) {
            return null;
        }
        return trungBinhNer(ds.stream().map(NEREntityResponse::text).toList());
    }

    
    public double[] tronSerch(double[] thanhPhan, double[] ner) {
        double tong = 1.0 - EmbeddingConfig.B0;

        double[] v = new double[thanhPhan.length];

        cong(v, thanhPhan, EmbeddingConfig.B0);

        if (ner != null && tong > 0) {
            cong(v, ner, tong);
        }

        return Vvec.ChuanHoaVec(v);
    }


}
