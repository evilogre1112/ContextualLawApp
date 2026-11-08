package com.example.Simple_logic.Service;

import java.util.ArrayList;
import java.util.HashSet;
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

import org.springframework.http.MediaType;


@Service 
public class ThanhPhanService {
    private final VanBanRepo vanBanRepo;
    private final ThanhPhanRepo thanhPhanRepo;
    private final NERService nerService;
    private final RestClient client = RestClient.builder()
                                                .requestFactory(new SimpleClientHttpRequestFactory())
                                                .build();
    private final Neo4jTemplate neo4jTemplate;

    public ThanhPhanService(VanBanRepo vanBanRepo,
                            ThanhPhanRepo thanhPhanRepo,
                            Neo4jTemplate neo4jTemplate,
                            NERService nerService){
        this.vanBanRepo = vanBanRepo ;
        this.thanhPhanRepo = thanhPhanRepo;
        this.neo4jTemplate = neo4jTemplate;
        this.nerService = nerService;
    }

    public int create(String articleId){
        VanBan saved = vanBanRepo.findById(articleId)
                                    .orElseThrow(()
                                    -> new RuntimeException("Không tìm thấy văn bản")
                                    );
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
                .body(new ParameterizedTypeReference<List<ThanhPhanRespone>>() {});
        
        Ttime.logTime("Tạo Thành Phần", start);
        if (thanhphan == null) {
            throw new RuntimeException("Python API không trả về dữ liệu");
        }

        Chuong chuong = null;
        Muc muc = null;
        Dieu dieu = null;
        Khoan khoan = null;
        Diem diem = null;
        List<ThanhPhan> all = new ArrayList<>();
        int total = 0;
        start = System.nanoTime();
        for (ThanhPhanRespone newtp : thanhphan) {

            if ("Chương".equals(newtp.getLoai_thanh_phan())) {

                chuong = new Chuong();
                chuong.setId(saved.getId() + "/C" + newtp.getStt());
                chuong.setStt(newtp.getStt());
                chuong.setVanBan(saved);
                chuong.setNoiDung(newtp.getNoi_dung());
                chuong.setLoaithanhphan(newtp.getLoai_thanh_phan());
                chuong.setEmbedding(embed(chuong.getNoiDung()));
                List<NER> ner = nerService.saveNer(chuong.getNoiDung());
                chuong.setNers(ner);

                all.add(chuong);

                muc = null;
                dieu = null;
                khoan = null;
                diem = null;

                total++;
            }

            else if ("Mục".equals(newtp.getLoai_thanh_phan())) {

                muc = new Muc();

                String parentId = (chuong != null)
                        ? chuong.getId()
                        : saved.getId();

                muc.setId(parentId + "/M" + newtp.getStt());
                muc.setStt(newtp.getStt());
                muc.setChuong(chuong);
                muc.setVanBan(saved);
                muc.setNoiDung(newtp.getNoi_dung());
                muc.setLoaithanhphan(newtp.getLoai_thanh_phan());
                muc.setEmbedding(embed(muc.getNoiDung()));
                List<NER> ner = nerService.saveNer(muc.getNoiDung());
                muc.setNers(ner);

                all.add(muc);

                dieu = null;
                khoan = null;
                diem = null;

                total++;
            }

            else if ("Điều".equals(newtp.getLoai_thanh_phan())) {

                dieu = new Dieu();

                String parentId = (muc != null)
                        ? muc.getId()
                        : (chuong != null ? chuong.getId() : saved.getId());

                dieu.setId(parentId + "/D" + newtp.getStt());
                dieu.setStt(newtp.getStt());
                dieu.setMuc(muc);
                dieu.setChuong(chuong);
                dieu.setVanBan(saved);
                dieu.setNoiDung(newtp.getNoi_dung());
                dieu.setLoaithanhphan(newtp.getLoai_thanh_phan());
                dieu.setEmbedding(embed(dieu.getNoiDung()));
                List<NER> ner = nerService.saveNer(dieu.getNoiDung());
                dieu.setNers(ner);

                all.add(dieu);

                khoan = null;
                diem = null;

                total++;
            }

            else if ("Khoản".equals(newtp.getLoai_thanh_phan())) {

                if (dieu == null) {
                    throw new IllegalStateException(
                        "Phát hiện Khoản nhưng không có Điều cha"
                    );
                }

                khoan = new Khoan();
                khoan.setId(dieu.getId() + "/K" + newtp.getStt());
                khoan.setStt(newtp.getStt());
                khoan.setDieu(dieu);
                khoan.setVanBan(saved);
                khoan.setNoiDung(newtp.getNoi_dung());
                khoan.setLoaithanhphan(newtp.getLoai_thanh_phan());
                khoan.setEmbedding(embed(khoan.getNoiDung()));
                List<NER> ner = nerService.saveNer(khoan.getNoiDung());
                khoan.setNers(ner);

                all.add(khoan);

                diem = null;

                total++;
            }

            else if ("Điểm".equals(newtp.getLoai_thanh_phan())) {

                if (khoan == null) {
                    throw new IllegalStateException(
                        "Phát hiện Điểm nhưng không có Khoản cha"
                    );
                }

                diem = new Diem();
                diem.setId(khoan.getId() + "/dm" + newtp.getStt());
                diem.setStt(newtp.getStt());
                diem.setKhoan(khoan);
                diem.setVanBan(saved);
                diem.setNoiDung(newtp.getNoi_dung());
                diem.setLoaithanhphan(newtp.getLoai_thanh_phan());
                diem.setEmbedding(embed(diem.getNoiDung()));
                List<NER> ner = nerService.saveNer(diem.getNoiDung());
                diem.setNers(ner);

                all.add(diem);

                total++;
            }
        }
        Ttime.logTime("Lưu thành phần trước khi saveall", start);
        start = System.nanoTime();
        thanhPhanRepo.saveAll(all);
        Ttime.logTime("Lưu thành phần sau khi saveall", start);
        return total;
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


    public List<ThanhPhan> search(String query, int k) {
        List<Double> embedding = embed(query);
        if (embedding == null) {
            throw new IllegalStateException("Không tạo được embedding cho truy vấn");
        }

        String cypher = """
                CALL db.index.vector.queryNodes('thanh_phan_embedding', $soUngVien, $q)
                YIELD node, score
                WHERE score >= 0.75
                RETURN node
                ORDER BY score DESC
                """;
        List<ThanhPhan> raw = neo4jTemplate.findAll(
                                                    cypher,
                                                    Map.of("soUngVien", Math.max(k * 5, 30), "q", embedding),
                                                    ThanhPhan.class
                                                );
        Set<String> daThay = new HashSet<>();
        List<ThanhPhan> ketQua = new ArrayList<>();
        for (ThanhPhan tp : raw) {
            if (daThay.add(chuanHoa(tp.getNoiDung()))) {
                ketQua.add(tp);
                if (ketQua.size() == k) break;
            }
        }
        return ketQua;
    }

    private String chuanHoa(String s) {
        if (s == null) return "";
        return s.strip()
                .replaceFirst("^(\\d+\\.|\\p{L}\\))\\s*", "")
                .strip()
                .toLowerCase();
    }
}
