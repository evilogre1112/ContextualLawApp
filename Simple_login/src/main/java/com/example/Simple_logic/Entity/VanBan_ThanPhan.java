package com.example.Simple_logic.Entity;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import jakarta.validation.constraints.NotBlank;

@RelationshipProperties 
public class VanBan_ThanPhan {
    @RelationshipId
    private Long internalId; // Spring Data quản lý

    @Property("id")
    private String id;        // ID quan hệ trong schema

    @NotBlank
    @Property ("Tên loại quan hệ")
    private String tenLoaiQuanHe;

    @NotBlank
    @Property ("Loại quan hệ")
    private String loaiQuanHe;

    @Property ("Phạm vi")
    private String phamVi;

    @Property ("Ngày bắt đầu")
    private LocalDate ngayBatDau;

    @Property ("Ngày kết thúc")
    private LocalDate ngayKetThuc;

    @Property ("Căn cứ")
    private String canCu;

    @TargetNode 
    private ThanhPhan thanhPhan;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenLoaiQuanHe() {
        return tenLoaiQuanHe;
    }

    public void setTenLoaiQuanHe(String tenLoaiQuanHe) {
        this.tenLoaiQuanHe = tenLoaiQuanHe;
    }

    public String getLoaiQuanHe() {
        return loaiQuanHe;
    }

    public void setLoaiQuanHe(String loaiQuanHe) {
        this.loaiQuanHe = loaiQuanHe;
    }

    public String getPhamVi() {
        return phamVi;
    }

    public void setPhamVi(String phamVi) {
        this.phamVi = phamVi;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getCanCu() {
        return canCu;
    }

    public void setCanCu(String canCu) {
        this.canCu = canCu;
    }

    public ThanhPhan getThanhPhan() {
        return thanhPhan;
    }

    public void setThanhPhan(ThanhPhan thanhPhan) {
        this.thanhPhan = thanhPhan;
    }

    public Long getInternalId() {
        return internalId;
    }

    public void setInternalId(Long internalId) {
        this.internalId = internalId;
    }

    
}
