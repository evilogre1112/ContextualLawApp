package com.example.Simple_logic.Service.thanhphan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.ThanhPhanRespone;
import com.example.Simple_logic.Entity.Chuong;
import com.example.Simple_logic.Entity.Diem;
import com.example.Simple_logic.Entity.Dieu;
import com.example.Simple_logic.Entity.Khoan;
import com.example.Simple_logic.Entity.Muc;
import com.example.Simple_logic.Entity.NER;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Extention.Ttime;
import com.example.Simple_logic.Service.embedding.EmbeddingModelService;
import com.example.Simple_logic.Service.ner.NERService;

@Service
public class ThanhPhanCreationService {
    private final VanBanRepo vanBanRepo;
    private final NERService nerService;
    private final ThanhPhanRepo thanhPhanRepo;
     private final CayThanhPhanCache cayThanhPhanCache;
    private final EmbeddingModelService embeddingModelService; 
    private final RestClient client = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();

    
    public ThanhPhanCreationService(VanBanRepo vanBanRepo, 
                                    NERService nerService,
                                    EmbeddingModelService embeddingModelService,
                                    ThanhPhanRepo thanhPhanRepo,
                                    CayThanhPhanCache cayThanhPhanCache) {
        this.vanBanRepo = vanBanRepo;
        this.nerService = nerService;
        this.embeddingModelService = embeddingModelService;
        this.thanhPhanRepo = thanhPhanRepo;
        this.cayThanhPhanCache = cayThanhPhanCache;
    }

    private void Init(ThanhPhan tp, VanBan vanBan, ThanhPhanRespone response, String id) {
        tp.setId(id);
        tp.setStt(response.getStt());
        tp.setVanBan(vanBan);
        tp.setNoiDung(response.getNoi_dung());
        tp.setLoaithanhphan(response.getLoai_thanh_phan());

        tp.setEmbedding(embeddingModelService.embed(tp.getNoiDung()));

        List<NER> ner = nerService.saveNer(tp.getNoiDung());
        tp.setNers(ner);
    }

    public int create(String articleId) {
        VanBan saved = vanBanRepo.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bản"));

        String noiDung = saved.getNoiDung();

        if (noiDung == null || noiDung.isBlank()) {
            throw new IllegalArgumentException("Nội dung văn bản đang bị trống");
        }

        long start = System.nanoTime();

        List<ThanhPhanRespone> thanhphan = client.post()
                .uri("http://127.0.0.1:8000/Gettp")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", noiDung))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ThanhPhanRespone>>() {
                });

        Ttime.logTime("Tạo Thành Phần", start);

        if (thanhphan == null) {
            throw new RuntimeException("Python API không trả về dữ liệu");
        }

        Chuong chuong = null;
        Muc muc = null;
        Dieu dieu = null;
        Khoan khoan = null;

        List<ThanhPhan> all = new ArrayList<>();

        start = System.nanoTime();

        for (ThanhPhanRespone newtp : thanhphan) {

            String loai = newtp.getLoai_thanh_phan();
            String stt = String.valueOf(newtp.getStt());

            switch (loai) {

                case "Chương" -> {
                    chuong = new Chuong();

                    String id = saved.getId() + "/C" + stt;

                    Init(chuong, saved, newtp, id);

                    all.add(chuong);

                    muc = null;
                    dieu = null;
                    khoan = null;
                }

                case "Mục" -> {
                    muc = new Muc();

                    String parentId = (chuong != null)
                            ? chuong.getId()
                            : saved.getId();

                    String id = parentId + "/M" + stt;

                    Init(muc, saved, newtp, id);

                    muc.setChuong(chuong);

                    all.add(muc);

                    dieu = null;
                    khoan = null;
                }

                case "Điều" -> {
                    dieu = new Dieu();

                    String parentId;
                    if (muc != null) {
                        parentId = muc.getId();
                        dieu.setMuc(muc);
                    } else if (chuong != null) {
                        parentId = chuong.getId();
                        dieu.setChuong(chuong);
                    } else {
                        parentId = saved.getId();
                        dieu.setVanBan(saved);
                    }

                    String id = parentId + "/D" + stt;

                    Init(dieu, saved, newtp, id);

                    all.add(dieu);
                    khoan = null;
                }

                case "Khoản" -> {
                    if (dieu == null) {
                        throw new IllegalStateException(
                                "Phát hiện Khoản nhưng không có Điều cha");
                    }

                    khoan = new Khoan();

                    String id = dieu.getId() + "/K" + stt;

                    Init(khoan, saved, newtp, id);

                    khoan.setDieu(dieu);

                    all.add(khoan);
                }

                case "Điểm" -> {
                    if (khoan == null) {
                        throw new IllegalStateException(
                                "Phát hiện Điểm nhưng không có Khoản cha");
                    }

                    Diem diem = new Diem();

                    String id = khoan.getId() + "/dm" + stt;

                    Init(diem, saved, newtp, id);

                    diem.setKhoan(khoan);

                    all.add(diem);
                }

                default -> System.out.println(
                        "Bỏ qua loại thành phần không hỗ trợ: " + loai);
            }
        }

        Ttime.logTime("Khởi tạo thành phần", start);

        start = System.nanoTime();

        thanhPhanRepo.saveAll(all);
        cayThanhPhanCache.reload();

        Ttime.logTime("Lưu thành phần", start);

        return all.size();
    }

}
