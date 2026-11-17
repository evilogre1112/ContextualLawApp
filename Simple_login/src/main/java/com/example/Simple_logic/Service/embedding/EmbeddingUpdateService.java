package com.example.Simple_logic.Service.embedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.Simple_logic.Config.EmbeddingConfig;
import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extension.Vvec;
import com.example.Simple_logic.Service.thanhphan.CayThanhPhanCache;
import com.example.Simple_logic.Service.ner.NERService;
import com.example.Simple_logic.Service.ner.NerEmbeddingService;

@Service 
public class EmbeddingUpdateService {

    private  final CayThanhPhanCache cayThanhPhanCache;
    private final ThanhPhanRepo thanhPhanRepo ;
    private final NERService nerService;
    private final NerEmbeddingService nerEmbeddingService;
    private final ThanhPhanVectorService thanhPhanVectorService;
    
    
    public EmbeddingUpdateService(CayThanhPhanCache cayThanhPhanCache, 
        ThanhPhanRepo thanhPhanRepo,
        NERService nerService,
        NerEmbeddingService nerEmbeddingService,
        ThanhPhanVectorService thanhPhanVectorService) {
        this.cayThanhPhanCache = cayThanhPhanCache;
        this.thanhPhanRepo = thanhPhanRepo;
        this.nerService = nerService;
        this.nerEmbeddingService = nerEmbeddingService;
        this.thanhPhanVectorService = thanhPhanVectorService;
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

    // Chạy lần lượt các bước trộn embedding và lưu kết quả cuối cùng.
    public synchronized void capNhatEmbedding() {
        merEmbThanhPhanWithNER();
        merEmbThanhPhanWithNCon();
        luuEmbeddingCuoiCung();
    }

    /** Kết hợp embedding gốc với thông tin NER để tạo vector tạm cho từng thành phần. */
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

                double[] ner = nerEmbeddingService.averageNerEmbeddings(
                        danhSachNER.getOrDefault(tp.getId(), List.of()));

                double trongSoNER = 1.0
                        - EmbeddingConfig.ROOT_COMPONENT_WEIGHT
                        - EmbeddingConfig.CHILD_COMPONENT_WEIGHT;

                double trongSoGoc;
                double trongSoCon;

                if (ner != null) {
                    trongSoGoc = EmbeddingConfig.ROOT_COMPONENT_WEIGHT;
                    trongSoCon = EmbeddingConfig.CHILD_COMPONENT_WEIGHT;
                } else {
                    trongSoGoc = EmbeddingConfig.ROOT_COMPONENT_WEIGHT + trongSoNER / 2.0;
                    trongSoCon = EmbeddingConfig.CHILD_COMPONENT_WEIGHT + trongSoNER / 2.0;
                }

                double[] v1 = new double[goc.length];

                thanhPhanVectorService.addWeightedVector(v1, goc, trongSoGoc);

                if (ner != null) {
                    thanhPhanVectorService.addWeightedVector(v1, ner, trongSoNER);
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

    /** Bổ sung embedding của các thành phần con theo thứ tự từ cấp thấp lên cấp cao. */
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

                    double[] tpcon = thanhPhanVectorService.averageChildEmbeddings(
                            tp, cayThanhPhanCache.get(), embeddingTam);

                    if (tpcon == null) {
                        continue;
                    }

                    double trongSoCon = getTrongsotpcontam().getOrDefault(
                            tp.getId(),
                            EmbeddingConfig.CHILD_COMPONENT_WEIGHT);

                    thanhPhanVectorService.addWeightedVector(base, tpcon, trongSoCon);

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

    // Chuẩn hóa các vector tạm và ghi embedding cuối cùng vào kho dữ liệu.
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
