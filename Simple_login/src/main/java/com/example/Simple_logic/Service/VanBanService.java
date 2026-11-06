package com.example.Simple_logic.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.CreateArticleRequest;
import com.example.Simple_logic.Entity.VanBan;


@Service 
public class VanBanService {
    private final VanBanRepo vanBanRepo;

    public VanBanService(VanBanRepo vanBanRepo) {
        this.vanBanRepo = vanBanRepo;
    }
    
    public VanBan create(CreateArticleRequest request ){
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

        
        return vanBanRepo.add(vanBan);
    }

}
