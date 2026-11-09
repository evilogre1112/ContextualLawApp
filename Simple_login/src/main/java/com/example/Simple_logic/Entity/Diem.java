package com.example.Simple_logic.Entity;

import java.time.LocalDate;


import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;


@Node(primaryLabel = "Điểm", labels = { "Thành phần" })
public class Diem extends ThanhPhan {

    @Property("Ngày bắt đầu")
    private LocalDate ngayBatDau;

    @Property("Ngày kết thúc")
    private LocalDate ngayKetThuc;

    @Relationship(type = "Thuộc Khoản", direction = Relationship.Direction.OUTGOING)
    private Khoan khoan;

    public Diem() {
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

    public Khoan getKhoan() {
        return khoan;
    }

    public void setKhoan(Khoan khoan) {
        this.khoan = khoan;
    }

    

}
