package com.example.Simple_logic.Service.vanban;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.AppMessage;
import com.example.Simple_logic.DTO.CreateArticleRequest;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Service.embedding.EmbeddingUpdateService;
import com.example.Simple_logic.Service.thanhphan.ThanhPhanCreationService;

import jakarta.transaction.Transactional;


@Service 
public class VanBanService {
    private final ThanhPhanCreationService thanhPhanCreationService;
    private final VanBanRepo vanBanRepo;
    private  final EmbeddingUpdateService embeddingUpdateService;

    public VanBanService(VanBanRepo vanBanRepo, 
                        ThanhPhanCreationService thanhPhanCreationService,
                        EmbeddingUpdateService embeddingUpdateService) {
        this.vanBanRepo = vanBanRepo;
        this.thanhPhanCreationService = thanhPhanCreationService;
        this.embeddingUpdateService = embeddingUpdateService;
    }
    
    @Transactional 
    public ResponseEntity<?> create(CreateArticleRequest request ){
        String id = request.id();

        VanBan vanBan = new VanBan();

        vanBan.setId(id);
        vanBan.setNoiDung(request.noiDung());
        vanBan.setTenVanBan(request.tenVanBan());
        vanBan.setUrl(request.url());
        vanBan.setSoHieu(request.soHieu());
        vanBan.setNganh(request.nganh());
        vanBan.setLinhVuc(request.linhVuc());
        vanBan.setChucDanh(request.chucDanh());
        vanBan.setTinhTrangHieuLuc(request.tinhTrangHieuLuc());
        vanBan.setLoaiVanBan(request.loaiVanBan());
        vanBan.setNgayBanHanh(request.ngayBanHanh());
        vanBan.setNgayCoHieuLuc(request.ngayCoHieuLuc());
        vanBan.setNgayHetHieuLuc(request.ngayHetHieuLuc());
        vanBan.setCoQuanBanHanh(request.coQuanBanHanh());
        vanBan.setNguoiKy(request.nguoiKy());

        vanBan.setVanBan_VanBans(request.vanBan_VanBans() == null ? List.of() : request.vanBan_VanBans());
        vanBan.setVanBan_ThanPhans(request.vanBan_ThanPhans() == null ? List.of() : request.vanBan_ThanPhans());
        List<AppMessage> msg = new ArrayList<>();
        try{
            vanBanRepo.add(vanBan);
            msg.add(new AppMessage("success", "Tạo văn bản thành công"));
        }catch(Exception e){
            throw new RuntimeException("Lỗi văn bản: " + e.getMessage(),e);
        }
          
        try{
            int total = thanhPhanCreationService.create(vanBan.getId());
            msg.add(new AppMessage("success", "Tạo thành phần thành công"));
            msg.add(new AppMessage("totalComponents", total));
        }catch(Exception e){
            throw new RuntimeException("Lỗi phân tích thành phần: " + e.getMessage() ,e);
        }
        try {
            embeddingUpdateService.capNhatEmbedding();

            msg.add(new AppMessage("success", "Merge embedding thành công"));

            return ResponseEntity.ok(
                Map.of("status", "Hoàn tất",
                    "messages", msg
                )
            );

        } catch (RuntimeException e) {
            msg.add(new AppMessage("error", "Merge embedding thất bại"));
            return ResponseEntity.internalServerError()
                .body(Map.of("status", "Thất bại",
                            "messages", msg 
                ));
        }
    }

}
