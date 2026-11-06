package com.example.Simple_logic.Entity;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import jakarta.validation.constraints.Min;


@Node (primaryLabel = "Điều", labels = {"Thành phần"})
public class Dieu extends ThanhPhan {

    @Property ("Ngày bắt đầu")
    private LocalDate ngayBatDau ;

    @Property ("Ngày kết thúc")
    private LocalDate ngayKetThuc ;

    @Min(1)
    @Property ("STT")
    private int stt;

    @Property("Nội dung")
    private String noiDung;

    @Relationship (
        type = "Thành phần thuộc Văn bản",
        direction = Relationship.Direction.OUTGOING
    )   
    private VanBan vanBan;


    public void setChuong(Chuong chuong) {
        this.chuong = chuong;
    }

    @Relationship (
        type = "Thuộc Chương",
        direction = Relationship.Direction.OUTGOING
    )   
    private Chuong chuong;

    @Relationship (
        type = "Thuộc Mục",
        direction = Relationship.Direction.OUTGOING
    )   
    private Muc muc;


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

    public int getStt() {
        return stt;
    }

    public void setStt(int stt) {
        this.stt = stt;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public VanBan getVanBan() {
        return vanBan;
    }

    public void setVanBan(VanBan vanBan) {
        this.vanBan = vanBan;
    }

    public Chuong getChuong() {
        return chuong;
    }

    public Muc getMuc() {
        return muc;
    }

    public void setMuc(Muc muc) {
        this.muc = muc;
    }

    
    
}
