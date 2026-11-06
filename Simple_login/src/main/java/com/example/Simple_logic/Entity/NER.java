package com.example.Simple_logic.Entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import jakarta.validation.constraints.NotBlank;

@Node (primaryLabel ="NER")
public class NER {
    @Id 
    private String id ;

    @NotBlank
    @Property ("Loại NER")
    private String loaiNER;

    @NotBlank
    @Property ("Tên NER")
    private String tenNER;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoaiNER() {
        return loaiNER;
    }

    public void setLoaiNER(String loaiNER) {
        this.loaiNER = loaiNER;
    }

    public String getTenNER() {
        return tenNER;
    }

    public void setTenNER(String tenNER) {
        this.tenNER = tenNER;
    }

    

}
