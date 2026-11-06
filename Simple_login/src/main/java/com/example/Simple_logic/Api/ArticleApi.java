package com.example.Simple_logic.Api;

import com.example.Simple_logic.DAO.NerRepo;
import java.net.URI;
import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.Entity.NER;
import com.example.Simple_logic.Entity.VanBan;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.Simple_logic.DTO.CreateArticleRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/api/Article")
public class ArticleApi
{
    private final NerRepo nerrepo;
    private final VanBanRepo vanBanRepo;
    private final NerController nerController;
    
    public ArticleApi(VanBanRepo vanBanRepo,NerController nerController, NerRepo nerrepo){
        this.vanBanRepo = vanBanRepo ;
        this.nerController = nerController;
        this.nerrepo = nerrepo;
    }

    @PostMapping
    public ResponseEntity<?> addArticle(@Valid @RequestBody CreateArticleRequest request ){
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

        
        VanBan saved =  vanBanRepo.themVanBan(vanBan);
        ResponseEntity<?> nerResponse = nerController.extract(new NerController.NerRequest(saved.getNoiDung()));
        if (!nerResponse.getStatusCode().is2xxSuccessful()
                || nerResponse.getBody() == null) {
            return ResponseEntity.status(502)
                    .body("Dịch vụ NER không trả về kết quả thành công: " + nerResponse.getBody());
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.valueToTree(nerResponse.getBody());
        JsonNode entities = root.get("entities");
        if (!entities.isArray()) {
            return ResponseEntity.status(502)
                    .body("Phản hồi NER không có mảng entities: " + root);
        }
        for (JsonNode entity : entities) {
            NER ner = new NER();

            ner.setLoaiNER(entity.get("label").asText());
            ner.setTenNER(entity.get("text").asText());

            nerrepo.themNER(ner);

        }
        return ResponseEntity.created(URI.create("/api/Article/" + saved.getId())).body(saved);
    }
    
}
