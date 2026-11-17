package com.example.Simple_logic.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Simple_logic.Config.EmbeddingConfig;
import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extention.Vvec;

@Service 
public class EmbeddingUpdateService {

    private  final CayThanhPhanCache cayThanhPhanCache;
    private final ThanhPhanRepo thanhPhanRepo ;
    private final NERService nerService;
    private  final EmbeddingService embeddingService;
    
    
    public EmbeddingUpdateService(CayThanhPhanCache cayThanhPhanCache, 
        ThanhPhanRepo thanhPhanRepo,
        NERService nerService,
        EmbeddingService embeddingService) {
        this.cayThanhPhanCache = cayThanhPhanCache;
        this.thanhPhanRepo = thanhPhanRepo;
        this.nerService = nerService;
        this.embeddingService = embeddingService;
    }

    private final Map<String, double[]> embeddingTam = new HashMap<>();
    private final Map<String, Double> trongSoTPConTam = new HashMap<>();

    @Value("${embedding.tinh-lai-tat-ca:false}")
    private volatile boolean tinhLaiTatCa;

    public Map<String, double[]> getEmbeddingtam() {
        return embeddingTam;
    }

    public Map<String, Double> getTrongsotpcontam() {
        return trongSoTPConTam;
    }

    public boolean getTinhLaiTatCa() { return tinhLaiTatCa; }
    public void setTinhLaiTatCa(boolean v) { this.tinhLaiTatCa = v; }

    public synchronized void capNhatEmbedding() {
        merEmbThanhPhanWithNER();
        merEmbThanhPhanWithNCon();
        luuEmbeddingCuoiCung();
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

                double[] ner = embeddingService.trungBinhNer(danhSachNER.getOrDefault(tp.getId(), List.of()));

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

                embeddingService.cong(v1, goc, trongSoGoc);

                if (ner != null) {
                    embeddingService.cong(v1, ner, trongSoNER);
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

                    double[] tpcon = embeddingService.getTrungBinhTPConEmbed(tp, cayThanhPhanCache.get(),embeddingTam);

                    if (tpcon == null) {
                        continue;
                    }

                    double trongSoCon = getTrongsotpcontam().getOrDefault(tp.getId(), EmbeddingConfig.A1);

                    embeddingService.cong(base, tpcon, trongSoCon);

                    getEmbeddingtam().put(tp.getId(), base);

                    soLuong++;

                } catch (Exception e) {
                    System.err.println("Lỗi trộn con cho " + tp.getId() + ": " + e.getMessage());
                }
            }

            System.out.println(
                    "Đã xử lý tầng " + loai + ", số thành phần được trộn: " + soLuong);
        }
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
}
