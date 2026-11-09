package com.example.Simple_logic.Entity;

import java.time.LocalDate;

import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;



@Node (primaryLabel = "Mục", labels = {"Thành phần"})
public class Muc extends ThanhPhan {

    @Property ("Ngày bắt đầu")
    private LocalDate ngayBatDau ;

    @Property ("Ngày kết thúc")
    private LocalDate ngayKetThuc ;

    @Relationship (
        type = "Thành phần thuộc Văn bản",
        direction = Relationship.Direction.OUTGOING
    )   
    private VanBan vanBan;


    public void setChuong(Chuong chuong) {
        this.chuong = chuong;
    }

    @Relationship (
        type = "Thuộc Một Chương",
        direction = Relationship.Direction.OUTGOING
    )   
    private Chuong chuong;


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

    public VanBan getVanBan() {
        return vanBan;
    }

    public void setVanBan(VanBan vanBan) {
        this.vanBan = vanBan;
    }

    public Chuong getChuong() {
        return chuong;
    }

    
}
