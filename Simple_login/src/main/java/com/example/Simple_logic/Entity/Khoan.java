package com.example.Simple_logic.Entity;

import java.time.LocalDate;


import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import jakarta.validation.constraints.Min;


@Node(primaryLabel = "Khoản", labels = { "Thành phần" })
public class Khoan extends ThanhPhan {

    @Property("Ngày bắt đầu")
    private LocalDate ngayBatDau;

    @Property("Ngày kết thúc")
    private LocalDate ngayKetThuc;

    @Min(1)
    @Property("STT")
    private int stt;

    @Relationship(type = "Thành phần thuộc Văn bản", direction = Relationship.Direction.OUTGOING)
    private VanBan vanBan;


    @Relationship(type = "Thuộc Điều", direction = Relationship.Direction.OUTGOING)
    private Dieu dieu;


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

    public VanBan getVanBan() {
        return vanBan;
    }

    public void setVanBan(VanBan vanBan) {
        this.vanBan = vanBan;
    }

    public Dieu getDieu() {
        return dieu;
    }

    public void setDieu(Dieu dieu) {
        this.dieu = dieu;
    }

   

}
