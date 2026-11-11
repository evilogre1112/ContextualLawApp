package com.example.Simple_logic.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.DAO.NerRepo;
import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.EmbeddingResponse;
import com.example.Simple_logic.DTO.NEREntityResponse;
import com.example.Simple_logic.DTO.QuanHe;
import com.example.Simple_logic.DTO.ThanhPhanRespone;
import com.example.Simple_logic.Entity.Chuong;
import com.example.Simple_logic.Entity.Diem;
import com.example.Simple_logic.Entity.Dieu;
import com.example.Simple_logic.Entity.Khoan;
import com.example.Simple_logic.Entity.Muc;
import com.example.Simple_logic.Entity.NER;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Extention.*;

import org.springframework.http.MediaType;
import org.springframework.data.neo4j.core.Neo4jClient;

@Service
public class ThanhPhanService {
    private final VanBanRepo vanBanRepo;
    private final ThanhPhanRepo thanhPhanRepo;
    private final NERService nerService;
    private final NerRepo nerRepo;
    private final RestClient client = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();

    private final Map<String, List<Double>> embeddingTam = new HashMap<>();
    private final Map<String, Double> trongSoTPConTam = new HashMap<>();

    public ThanhPhanService(VanBanRepo vanBanRepo,
            ThanhPhanRepo thanhPhanRepo,
            Neo4jTemplate neo4jTemplate,
            NERService nerService,
            Neo4jClient neo4jClient,
            NerRepo nerRepo) {
        this.vanBanRepo = vanBanRepo;
        this.thanhPhanRepo = thanhPhanRepo;
        this.nerService = nerService;
        this.nerRepo = nerRepo;
    }

    private void Init(ThanhPhan tp, VanBan vanBan, ThanhPhanRespone response, String id) {
        tp.setId(id);
        tp.setStt(response.getStt());
        tp.setVanBan(vanBan);
        tp.setNoiDung(response.getNoi_dung());
        tp.setLoaithanhphan(response.getLoai_thanh_phan());

        tp.setEmbedding(embed(tp.getNoiDung()));

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

                    String parentId = (muc != null)
                            ? muc.getId()
                            : (chuong != null ? chuong.getId() : saved.getId());

                    String id = parentId + "/D" + stt;

                    Init(dieu, saved, newtp, id);

                    dieu.setMuc(muc);
                    dieu.setChuong(chuong);

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

        Ttime.logTime("Lưu thành phần", start);

        return all.size();
    }

    private List<Double> embed(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        long start = System.nanoTime();
        EmbeddingResponse response = client.post()
                .uri("http://127.0.0.1:8000/generate-embedding")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .body(EmbeddingResponse.class);
        Ttime.logTime("Tạo embedding", start);
        return response != null ? response.getEmbedding() : null;
    }

    private static final double NGUONG_SEARCH = 0.4;

    public List<ThanhPhan> search(String query, int k) {

        List<Double> embeddingGoc = embed(query);

        if (embeddingGoc == null) {
            throw new IllegalStateException(
                    "Không tạo được embedding cho truy vấn");
        }

        List<Double> embeddingTimKiem = embeddingGoc;

        try {
            List<Double> vectorner = getEmbedNERInText(query);

            if (vectorner != null) {
                embeddingTimKiem = tronSerch(embeddingGoc, vectorner);
            }
        } catch (Exception e) {
            System.err.println(
                    "Bỏ qua NER của câu hỏi: " + e.getMessage());
        }

        int soUngVien = Math.max(k * 10, 100);

        Map<String, ThanhPhan> ungVien = new LinkedHashMap<>();

        List<ThanhPhan> dsEmbedding = thanhPhanRepo.findEmbedding(embeddingTimKiem, soUngVien);

        List<ThanhPhan> dsEmbeddingGoc = thanhPhanRepo.findEmbeddingGoc(embeddingGoc, soUngVien);

        // Gộp, tránh trùng ID
        for (ThanhPhan tp : dsEmbedding) {
            ungVien.put(tp.getId(), tp);
        }

        for (ThanhPhan tp : dsEmbeddingGoc) {
            ungVien.put(tp.getId(), tp);
        }

        // Chấm điểm lại
        List<Map.Entry<ThanhPhan, Double>> ketQua = new ArrayList<>();

        for (ThanhPhan tp : ungVien.values()) {

            List<Double> goc = tp.getEmbeddingGoc();
            List<Double> tongHop = tp.getEmbedding();

            double scoreGoc = -1;
            double scoreTongHop = -1;

            if (goc != null) {
                scoreGoc = Vvec.cosineSimilarity(embeddingGoc, goc);
            }

            if (tongHop != null) {
                scoreTongHop = Vvec.cosineSimilarity(embeddingTimKiem, tongHop);
            }

            double score = Math.max(scoreGoc, scoreTongHop);

            if (score >= NGUONG_SEARCH) {
                ketQua.add(Map.entry(tp, score));
            }
        }

        // Sắp xếp theo điểm tương đồng
        ketQua.sort(
                Map.Entry.<ThanhPhan, Double>comparingByValue().reversed());

        // Loại bỏ nội dung trùng nhau
        Set<String> daThay = new HashSet<>();
        List<ThanhPhan> danhSach = new ArrayList<>();

        for (Map.Entry<ThanhPhan, Double> entry : ketQua) {

            ThanhPhan tp = entry.getKey();

            if (daThay.add(Normalize.chuanHoa(tp.getNoiDung()))) {
                danhSach.add(tp);

                if (danhSach.size() == k) {
                    break;
                }
            }
        }

        return danhSach;
    }

    private final Map<String, List<Double>> embeddingCache = new ConcurrentHashMap<>();

    private List<List<Double>> embedCoCache(List<String> tenNers) {
        List<String> chuaCoCache = tenNers.stream()
                .filter(ten -> !embeddingCache.containsKey(ten))
                .distinct()
                .toList();
        if (!chuaCoCache.isEmpty()) {
            List<List<Double>> embeddings = embedBatch(chuaCoCache);
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

    private List<List<Double>> embedBatch(List<String> texts) {
        List<List<Double>> result = client.post()
                .uri("http://127.0.0.1:8000/embed/batch")
                .body(Map.of("texts", texts))
                .retrieve()
                .body(new ParameterizedTypeReference<List<List<Double>>>() {
                });

        if (result == null) {
            throw new IllegalStateException("Python không trả về embedding");
        }

        return result;
    }

    private List<Double> trungBinhNer(List<String> tenNER) {
        List<String> ds = tenNER.stream()
                .filter(s -> s != null && !s.isBlank())
                .toList();
        if (ds.isEmpty()) {
            return null;
        }
        List<List<Double>> dsEmbedding = embedCoCache(ds);

        double[] tb = new double[768];
        for (int j = 0; j < dsEmbedding.size(); j++) {
            List<Double> e = dsEmbedding.get(j);
            String ten = ds.get(j);
            double n = 0;
            for (double x : e)
                n += x * x;
            n = Math.sqrt(n);
            if (n == 0) {
                throw new IllegalStateException("Vector NER có độ dài 0: " + ten);
            }
            for (int i = 0; i < 768; i++) {
                tb[i] += e.get(i) / n / ds.size();
            }
        }

        return Vvec.ChuanHoaVec(tb);
    }

    private List<Double> getEmbedNERInText(String text) {
        List<NEREntityResponse> ds = nerService.exNer(text);
        if (ds == null) {
            return null;
        }
        return trungBinhNer(ds.stream().map(NEREntityResponse::text).toList());
    }

    private List<Double> getTrungBinhTPConEmbed(ThanhPhan tp, Map<String, List<String>> danhSachCon) {
        List<String> dsIdCon = danhSachCon.get(tp.getId());

        if (dsIdCon == null || dsIdCon.isEmpty()) {
            return null;
        }

        int n = -1;
        double[] tong = null;

        for (String idCon : dsIdCon) {
            List<Double> emb = embeddingTam.get(idCon);

            if (emb == null) {
                continue;
            }

            if (tong == null) {
                n = emb.size();
                tong = new double[n];
            }

            cong(tong, emb, 1.0);
        }

        if (tong == null) {
            return null;
        }

        return Vvec.ChuanHoaVec(tong);
    }

    private Map<String, List<String>> layTatCaTPCon() {
        List<QuanHe> rows = thanhPhanRepo.getDanhSachQuanHe();

        Map<String, List<String>> ketQua = new HashMap<>();

        for (QuanHe row : rows) {
            ketQua.computeIfAbsent(row.tpId(), k -> new ArrayList<>())
                    .add(row.ten());
        }

        return ketQua;
    }

    private Map<String, List<String>> layTatCaNER() {

        List<QuanHe> rows = nerRepo.layTatCaNERThuocTP();

        Map<String, List<String>> ketQua = new HashMap<>();

        for (QuanHe row : rows) {
            ketQua.computeIfAbsent(
                    row.tpId(), k -> new ArrayList<>()).add(row.ten());
        }

        return ketQua;
    }

    /** Tính emb tạm */
    public void merEmbThanhPhanWithNER() {
        Map<String, List<String>> danhSachNER = layTatCaNER();
        embeddingTam.clear();
        trongSoTPConTam.clear();

        List<ThanhPhan> dsTp = thanhPhanRepo.getThanhPhanCoEmb();

        for (ThanhPhan tp : dsTp) {
            try {
                List<Double> goc = tp.getEmbeddingGoc();

                if (goc == null) {
                    goc = tp.getEmbedding();
                }

                List<Double> ner = trungBinhNer(danhSachNER.getOrDefault(tp.getId(), List.of()));

                double trongSoNER = 1.0 - A0 - A1;

                double trongSoGoc;
                double trongSoCon;

                if (ner != null) {
                    trongSoGoc = A0;
                    trongSoCon = A1;
                } else {
                    trongSoGoc = A0 + trongSoNER / 2.0;
                    trongSoCon = A1 + trongSoNER / 2.0;
                }

                double[] v1 = new double[goc.size()];

                cong(v1, goc, trongSoGoc);

                if (ner != null) {
                    cong(v1, ner, trongSoNER);
                }

                List<Double> ketQua = new ArrayList<>(v1.length);

                for (double x : v1) {
                    ketQua.add(x);
                }

                embeddingTam.put(tp.getId(), ketQua);
                trongSoTPConTam.put(tp.getId(), trongSoCon);

            } catch (Exception e) {
                System.err.println(
                        "Lỗi trộn vector cho " + tp.getId()
                                + ": " + e.getMessage());
            }
        }

        System.out.println(
                "Đã tạo vector trung gian cho " + embeddingTam.size() + " thành phần.");
    }

    /** Hàm này sử dụng sau khi nó các thành phần đã trộn với NER */
    public void merEmbThanhPhanWithNCon() {
        Map<String, List<String>> danhSachCon = layTatCaTPCon();
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
                    List<Double> base = embeddingTam.get(tp.getId());

                    if (base == null) {
                        continue;
                    }

                    List<Double> tpcon = getTrungBinhTPConEmbed(tp, danhSachCon);

                    if (tpcon == null) {
                        continue;
                    }

                    double[] v2 = base.stream()
                            .mapToDouble(Double::doubleValue)
                            .toArray();

                    double trongSoCon = trongSoTPConTam.getOrDefault(tp.getId(), A1);

                    cong(v2, tpcon, trongSoCon);

                    List<Double> ketQua = new ArrayList<>(v2.length);

                    for (double x : v2) {
                        ketQua.add(x);
                    }

                    embeddingTam.put(tp.getId(), ketQua);

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

        for (Map.Entry<String, List<Double>> entry : embeddingTam.entrySet()) {
            try {
                List<Double> vector = entry.getValue();

                List<Double> embeddingFinal = Vvec
                        .ChuanHoaVec(vector.stream().mapToDouble(Double::doubleValue).toArray());

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

    private static final double A0 = 0.55; // Trọng số vector thành phần gốc (giữ nét đặc trưng cốt lõi)
    private static final double A1 = 0.30; // Trọng số vector thành phần con (đóng góp ngữ cảnh bottom-up)
    // Trọng số NER = 1.0 - A0 - A1 = 0.15 (thực thể pháp lý)

    private static final double B0 = 0.80; // Trọng số câu truy vấn gốc (80% câu hỏi, 20% NER bổ trợ)

    private List<Double> tronSerch(List<Double> thanhPhan, List<Double> ner) {
        double tong = 1.0 - B0;

        double[] v = new double[thanhPhan.size()];

        cong(v, thanhPhan, B0);

        if (ner != null && tong > 0) {
            cong(v, ner, tong);
        }

        return Vvec.ChuanHoaVec(v);
    }

    /** v += w * (u / |u|) */
    private static void cong(double[] v, List<Double> u, double w) {
        if (u.size() != v.length) {
            throw new IllegalStateException(
                    "Số chiều lệch nhau: " + u.size() + " và " + v.length);
        }

        double norm = 0;
        for (double x : u)
            norm += x * x;
        norm = Math.sqrt(norm);

        if (norm == 0) {
            throw new IllegalStateException("Vector có độ dài 0");
        }

        for (int i = 0; i < v.length; i++) {
            v[i] += w * u.get(i) / norm;
        }
    }

}
