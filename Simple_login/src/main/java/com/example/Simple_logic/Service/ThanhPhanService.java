package com.example.Simple_logic.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.EmbeddingResponse;
import com.example.Simple_logic.DTO.Point;
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

@Service
public class ThanhPhanService {
    private final VanBanRepo vanBanRepo;
    private final ThanhPhanRepo thanhPhanRepo;
    private final NERService nerService;
    private final RestClient client = RestClient.builder()
            .requestFactory(new SimpleClientHttpRequestFactory())
            .build();
    private final EmbeddingService embeddingService ;
    private  final CayThanhPhanCache cayThanhPhanCache;
    public ThanhPhanService(VanBanRepo vanBanRepo,
            ThanhPhanRepo thanhPhanRepo,
            Neo4jTemplate neo4jTemplate,
            NERService nerService,
            EmbeddingService embeddingService,
            CayThanhPhanCache cayThanhPhanCache) {
        this.vanBanRepo = vanBanRepo;
        this.thanhPhanRepo = thanhPhanRepo;
        this.nerService = nerService;
        this.embeddingService = embeddingService;
        this.cayThanhPhanCache = cayThanhPhanCache;
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

    private double[] embed(String text) {
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

    private List<Integer> Rranking(List<Point> p) {
        if (p == null || p.isEmpty()) {
            return List.of();
        }
        List<List<Double>> listPoints = p.stream().map(point -> Arrays.asList(point.x(), point.y(), point.z())).toList();
        long start = System.nanoTime();
        Map<String, Object> requestBody = Map.of(
                "ListPoints", listPoints);
        Map<String, List<Integer>> response = client.post()
                .uri("http://127.0.0.1:8000/Rranking")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, List<Integer>>>() {
                });
        Ttime.logTime("Xếp hạng", start);
        return response != null ? response.get("Ranking") : List.of();
    }

    private static final double NGUONG_SEARCH = 0.65;

    public List<ThanhPhan> search(String query, int k) {

        double[] embeddingGoc = embed(query);

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

            double[] goc = tp.getEmbeddingGoc();
            double[] tongHop = tp.getEmbedding();

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
        ketQua.sort(Map.Entry.<ThanhPhan, Double>comparingByValue().reversed());

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

    private ThanhPhanRespone toResponse(ThanhPhan tp) {
        ThanhPhanRespone res = new ThanhPhanRespone();

        res.setId(tp.getId());
        res.setLoai_thanh_phan(tp.getLoaithanhphan());
        res.setStt(tp.getStt());
        res.setNoi_dung(tp.getNoiDung());

        return res;
    }

    public List<ThanhPhanRespone> search2(String query, int k) {
        double[] embeddingGoc = embed(query);

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

        List<Integer> R = Rranking(dsRank);
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
