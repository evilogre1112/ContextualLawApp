package com.example.Simple_logic.Entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Thành phần")
public abstract class ThanhPhan {
    @Id
    private String id;

    @Relationship ("Thành phần - Văn bản")
    private List<ThanhPhan_VanBan> thanhPhan_VanBans = new ArrayList<>() ;

    @Relationship ("Thành phần - Thành phần")
    private List<ThanhPhan_ThanhPhan> thanhPhan_ThanhPhans = new ArrayList<>() ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ThanhPhan_VanBan> getThanhPhan_VanBans() {
        return thanhPhan_VanBans;
    }

    public void setThanhPhan_VanBans(List<ThanhPhan_VanBan> thanhPhan_VanBans) {
        this.thanhPhan_VanBans = thanhPhan_VanBans;
    }

    public List<ThanhPhan_ThanhPhan> getThanhPhan_ThanhPhans() {
        return thanhPhan_ThanhPhans;
    }

    public void setThanhPhan_ThanhPhans(List<ThanhPhan_ThanhPhan> thanhPhan_ThanhPhans) {
        this.thanhPhan_ThanhPhans = thanhPhan_ThanhPhans;
    }

    
}