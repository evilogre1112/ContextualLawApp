package com.example.Simple_logic.Entity;

import java.time.LocalDate;


import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;




@Node (primaryLabel = "Chương", labels = {"Thành phần"})
public class Chuong extends ThanhPhan{
    
    @Property ("Ngày bắt đầu")
    private LocalDate ngayBatDau ;

    @Property ("Ngày kết thúc")
    private LocalDate ngayKetThuc ;

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

    
}
