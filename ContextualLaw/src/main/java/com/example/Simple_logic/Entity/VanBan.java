package com.example.Simple_logic.Entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import jakarta.validation.constraints.NotBlank;

@Node(primaryLabel = "VBPL", labels = {"Văn bản"})
public class VanBan {

    @Id
    private String id;

    @Property("Nội dung")
    private String noiDung;

    @NotBlank 
    @Property("Tên văn bản")
    private String tenVanBan;

    @NotBlank
    @Property("URL")
    private String url;

    @Property("Số hiệu")
    private String soHieu;

    @Property("Ngành")
    private String nganh;

    @Property("Lĩnh vực")
    private String linhVuc;

    @Property("Chức danh")
    private String chucDanh;

    @Property("Loại văn bản")
    private String loaiVanBan;

    @Property("Tình trạng hiệu lực")
    private String tinhTrangHieuLuc;

    @Property("Ngày ban hành")
    private LocalDate ngayBanHanh;

    @Property("Ngày có hiệu lực")
    private LocalDate ngayCoHieuLuc;

    @Property("Ngày hết hiệu lực")
    private LocalDate ngayHetHieuLuc;

    @Property("Cơ quan ban hành")
    private String coQuanBanHanh;

    @Property("Người ký")
    private String nguoiKy;

    @Relationship(
        type = "Văn bản - Văn bản",
        direction = Relationship.Direction.OUTGOING
    )
    private List<VanBan_VanBan> vanBan_VanBans = new ArrayList<>();

    @Relationship(
        type = "Văn bản - Thành phần",
        direction = Relationship.Direction.OUTGOING
    )
    private List<VanBan_ThanPhan> vanBan_ThanPhans = new ArrayList<>();

    public VanBan() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getTenVanBan() {
        return tenVanBan;
    }

    public void setTenVanBan(String tenVanBan) {
        this.tenVanBan = tenVanBan;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSoHieu() {
        return soHieu;
    }

    public void setSoHieu(String soHieu) {
        this.soHieu = soHieu;
    }

    public String getNganh() {
        return nganh;
    }

    public void setNganh(String nganh) {
        this.nganh = nganh;
    }

    public String getLinhVuc() {
        return linhVuc;
    }

    public void setLinhVuc(String linhVuc) {
        this.linhVuc = linhVuc;
    }

    public String getChucDanh() {
        return chucDanh;
    }

    public void setChucDanh(String chucDanh) {
        this.chucDanh = chucDanh;
    }

    public String getLoaiVanBan() {
        return loaiVanBan;
    }

    public void setLoaiVanBan(String loaiVanBan) {
        this.loaiVanBan = loaiVanBan;
    }


    public LocalDate getNgayCoHieuLuc() {
        return ngayCoHieuLuc;
    }

    public void setNgayCoHieuLuc(LocalDate ngayCoHieuLuc) {
        this.ngayCoHieuLuc = ngayCoHieuLuc;
    }

    public String getCoQuanBanHanh() {
        return coQuanBanHanh;
    }

    public void setCoQuanBanHanh(String coQuanBanHanh) {
        this.coQuanBanHanh = coQuanBanHanh;
    }

    public String getNguoiKy() {
        return nguoiKy;
    }

    public void setNguoiKy(String nguoiKy) {
        this.nguoiKy = nguoiKy;
    }

    public List<VanBan_VanBan> getVanBan_VanBans() {
        return vanBan_VanBans;
    }

    public void setVanBan_VanBans(List<VanBan_VanBan> vanBan_VanBans) {
        this.vanBan_VanBans = vanBan_VanBans;
    }

    public List<VanBan_ThanPhan> getVanBan_ThanPhans() {
        return vanBan_ThanPhans;
    }

    public void setVanBan_ThanPhans(List<VanBan_ThanPhan> vanBan_ThanPhans) {
        this.vanBan_ThanPhans = vanBan_ThanPhans;
    }

    public String getTinhTrangHieuLuc() {
        return tinhTrangHieuLuc;
    }

    public void setTinhTrangHieuLuc(String tinhTrangHieuLuc) {
        this.tinhTrangHieuLuc = tinhTrangHieuLuc;
    }

    public LocalDate getNgayBanHanh() {
        return ngayBanHanh;
    }

    public void setNgayBanHanh(LocalDate ngayBanHanh) {
        this.ngayBanHanh = ngayBanHanh;
    }

    public LocalDate getNgayHetHieuLuc() {
        return ngayHetHieuLuc;
    }

    public void setNgayHetHieuLuc(LocalDate ngayHetHieuLuc) {
        this.ngayHetHieuLuc = ngayHetHieuLuc;
    }

    

}