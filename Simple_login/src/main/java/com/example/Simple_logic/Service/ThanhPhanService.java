package com.example.Simple_logic.Service;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.Simple_logic.Controller.NerController;
import com.example.Simple_logic.DAO.ThanhPhanRepo;
import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.ThanhPhanRespone;
import com.example.Simple_logic.Entity.Chuong;
import com.example.Simple_logic.Entity.Diem;
import com.example.Simple_logic.Entity.Dieu;
import com.example.Simple_logic.Entity.Khoan;
import com.example.Simple_logic.Entity.Muc;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Entity.VanBan;
import org.springframework.http.MediaType;

import jakarta.transaction.Transactional;

@Service 
public class ThanhPhanService {
    private final VanBanRepo vanBanRepo;
    private final ThanhPhanRepo thanhPhanRepo;
    private final RestClient client = RestClient.builder()
                                                .requestFactory(new SimpleClientHttpRequestFactory())
                                                .build();
    public ThanhPhanService(VanBanRepo vanBanRepo,NerController nerController,ThanhPhanRepo thanhPhanRepo){
        this.vanBanRepo = vanBanRepo ;
        this.thanhPhanRepo = thanhPhanRepo;
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

        List<ThanhPhanRespone> thanhphan = client.post()
                .uri("http://127.0.0.1:8000/Gettp")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", noiDung))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ThanhPhanRespone>>() {});
        
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

        for (ThanhPhanRespone newtp : thanhphan) {

            if ("Chương".equals(newtp.getLoai_thanh_phan())) {

                chuong = new Chuong();
                chuong.setId(saved.getId() + "/C" + newtp.getStt());
                chuong.setStt(newtp.getStt());
                chuong.setVanBan(saved);
                chuong.setNoiDung(newtp.getNoi_dung());
                chuong.setLoaithanhphan(newtp.getLoai_thanh_phan());

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

                all.add(diem);

                total++;
            }
        }
        thanhPhanRepo.saveAll(all);
        return total;
    }

}
