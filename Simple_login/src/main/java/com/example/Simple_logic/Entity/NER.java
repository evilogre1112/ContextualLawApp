package com.example.Simple_logic.Entity;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import jakarta.validation.constraints.NotBlank;

@Node (primaryLabel ="NER")
public class NER {
    @Id 
    @GeneratedValue(UUIDStringGenerator.class)
    private String id ;

    @NotBlank
    @Property ("Loại NER")
    private String loaiNER;

    @NotBlank
    @Property ("Tên NER")
    private String tenNER;


    public NER(String id, @NotBlank String loaiNER, @NotBlank String tenNER) {
        this.id = id;
        this.loaiNER = loaiNER;
        this.tenNER = tenNER;
    }

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
