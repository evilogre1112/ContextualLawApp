package com.example.Simple_logic.Service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.Config.EmbeddingConfig;
import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DTO.NEREntityResponse;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extention.Vvec;

@Service
public class EmbeddingService {
    public final ThanhPhanRepo thanhPhanRepo ;
    private double heSoCha = 0.3;
    private final Map<String, double[]> embeddingTam = new HashMap<>();
    private final Map<String, Double> trongSoTPConTam = new HashMap<>();
    private final Map<String, double[]> embeddingCache = new ConcurrentHashMap<>();
    private final NERService nerService;
    private  final CayThanhPhanCache cayThanhPhanCache;

    @Value("${embedding.tinh-lai-tat-ca:false}")
    private volatile boolean tinhLaiTatCa;
   
    private final RestClient client = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();

    EmbeddingService(ThanhPhanRepo thanhPhanRepo,
                     NERService nerService,
                     CayThanhPhanCache cayThanhPhanCache
    ) {
        this.thanhPhanRepo = thanhPhanRepo;
        this.nerService = nerService ;
        this.cayThanhPhanCache = cayThanhPhanCache;
    }

    public boolean getTinhLaiTatCa() { return tinhLaiTatCa; }
    public void setTinhLaiTatCa(boolean v) { this.tinhLaiTatCa = v; }
    
    public void merwithparent(ThanhPhan parent, double[] embedtpcon) {
        if (parent == null || parent.getEmbeddingGoc() == null) return;
        cong(embedtpcon, parent.getEmbeddingGoc(), heSoCha);
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
                .filter(ten -> !embeddingCache.containsKey(ten))
                .distinct()
                .toList();
        if (!chuaCoCache.isEmpty()) {
            List<double[]> embeddings = embedBatch(chuaCoCache);
            if (embeddings == null || embeddings.size() != chuaCoCache.size()) {
                throw new IllegalStateException("Số lượng embedding trả về không khớp số lượng NER");
            }
            for (int i = 0; i < chuaCoCache.size(); i++) {
                embeddingCache.put(chuaCoCache.get(i), embeddings.get(i));
            }
        }
        return tenNers.stream()
                .map(embeddingCache::get)
                .toList();
    }

    private double[] trungBinhNer(List<String> tenNER) {
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

    
    private List<double[]> embedBatch(List<String> texts) {
        List<double[]> result = client.post()
                .uri("http://127.0.0.1:8000/embed/batch")
                .body(Map.of("texts", texts))
                .retrieve()
                .body(new ParameterizedTypeReference<List<double[]>>() {
                });

        if (result == null) {
            throw new IllegalStateException("Python không trả về embedding");
        }

        return result;
    }

    public void luuEmbeddingCuoiCung() {
        List<Map<String, Object>> rows = new ArrayList<>();

        for (Map.Entry<String, double[]> entry : embeddingTam.entrySet()) {
            try {
                double[] vector = entry.getValue();

                double[] embeddingFinal = Vvec.ChuanHoaVec(vector);

                rows.add(Map.of(
                        "id", entry.getKey(),
                        "emb", embeddingFinal));

            } catch (Exception e) {
                System.err.println(
                        "Lỗi chuẩn hóa " + entry.getKey() + ": " + e.getMessage());
            }
        }

        if (!rows.isEmpty()) {
            thanhPhanRepo.updateAll(rows);
        }

        System.out.println(
                "Đã lưu " + rows.size() + " embedding cuối cùng.");
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

    /** Tính emb tạm */
    public void merEmbThanhPhanWithNER() {
        Map<String, List<String>> danhSachNER = nerService.layTatCaNER();
        embeddingTam.clear();
        trongSoTPConTam.clear();

        List<ThanhPhan> dsTp = tinhLaiTatCa ? thanhPhanRepo.getThanhPhanCoEmb() : thanhPhanRepo.getThanhPhanChuaCoEmbGoc();
        
        System.out.println("Chế độ " + (tinhLaiTatCa ? "tính lại tất cả" : "chỉ phần mới")+ ": " + dsTp.size() + " thành phần");

        for (ThanhPhan tp : dsTp) {
            try {
                double[] goc = tp.getEmbeddingGoc();

                if (goc == null) {
                    goc = tp.getEmbedding();
                }

                double[] ner = trungBinhNer(danhSachNER.getOrDefault(tp.getId(), List.of()));

                double trongSoNER = 1.0 - EmbeddingConfig.A0 - EmbeddingConfig.A1;

                double trongSoGoc;
                double trongSoCon;

                if (ner != null) {
                    trongSoGoc = EmbeddingConfig.A0;
                    trongSoCon = EmbeddingConfig.A1;
                } else {
                    trongSoGoc = EmbeddingConfig.A0 + trongSoNER / 2.0;
                    trongSoCon = EmbeddingConfig.A1 + trongSoNER / 2.0;
                }

                double[] v1 = new double[goc.length];

                cong(v1, goc, trongSoGoc);

                if (ner != null) {
                    cong(v1, ner, trongSoNER);
                }


                getEmbeddingtam().put(tp.getId(), v1);
                getTrongsotpcontam().put(tp.getId(), trongSoCon);

            } catch (Exception e) {
                System.err.println(
                        "Lỗi trộn vector cho " + tp.getId()
                                + ": " + e.getMessage());
            }
        }

        System.out.println(
                "Đã tạo vector trung gian cho " + getEmbeddingtam().size() + " thành phần.");
    }

     /** Hàm này sử dụng sau khi nó các thành phần đã trộn với NER */
    public void merEmbThanhPhanWithNCon() {
        List<String> thuTu = List.of(
                "Khoản",
                "Điều",
                "Mục",
                "Chương");

        for (String loai : thuTu) {

            List<ThanhPhan> dsTp = thanhPhanRepo.getThanhPhanTheoLoai(loai);

            int soLuong = 0;

            for (ThanhPhan tp : dsTp) {
                try {
                    double[] base = getEmbeddingtam().get(tp.getId());

                    if (base == null) {
                        continue;
                    }

                    double[] tpcon = getTrungBinhTPConEmbed(tp, cayThanhPhanCache.get(),embeddingTam);

                    if (tpcon == null) {
                        continue;
                    }

                    double[] v2 = base ;

                    double trongSoCon = getTrongsotpcontam().getOrDefault(tp.getId(), EmbeddingConfig.A1);

                    cong(v2, tpcon, trongSoCon);

                    getEmbeddingtam().put(tp.getId(), v2);

                    soLuong++;

                } catch (Exception e) {
                    System.err.println("Lỗi trộn con cho " + tp.getId() + ": " + e.getMessage());
                }
            }

            System.out.println(
                    "Đã xử lý tầng " + loai + ", số thành phần được trộn: " + soLuong);
        }
    }

    public synchronized void capNhatEmbedding() {
        merEmbThanhPhanWithNER();
        merEmbThanhPhanWithNCon();
        luuEmbeddingCuoiCung();
    }


    public ThanhPhanRepo getThanhPhanRepo() {
        return thanhPhanRepo;
    }

    public double getHeSoCha() {
        return heSoCha;
    }

    public void setHeSoCha(double heSoCha) {
        this.heSoCha = heSoCha;
    }

    public Map<String, double[]> getEmbeddingtam() {
        return embeddingTam;
    }

    public Map<String, Double> getTrongsotpcontam() {
        return trongSoTPConTam;
    }

    public Map<String, double[]> getEmbeddingcache() {
        return embeddingCache;
    }

    public NERService getNerService() {
        return nerService;
    }

    public RestClient getClient() {
        return client;
    }

    
}
