package com.example.Simple_logic.Entity;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;


@Node (primaryLabel = "Điều", labels = {"Thành phần"})
public class Dieu extends ThanhPhan {

    @Property ("Ngày bắt đầu")
    private LocalDate ngayBatDau ;

    @Property ("Ngày kết thúc")
    private LocalDate ngayKetThuc ;

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
