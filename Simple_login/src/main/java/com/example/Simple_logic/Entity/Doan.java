package com.example.Simple_logic.Entity;

import java.util.List;


import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Node(primaryLabel = "Đoạn")
public class Doan {
    @Id
    private String id;
    
    @Min(0)
    @Property("Bắt đầu")
    private int batDau;

    @Min(0)
    @Property("Kết thúc")
    private int ketThuc;

    @Property("Embedding")
    private List<@NotNull Float> embedding;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Relationship (
        type = "Nhắc đến NER",
        direction = Relationship.Direction.OUTGOING
    )
    private NER ner ;

    @Relationship (
        type = "Thuộc Thành phần",
        direction = Relationship.Direction.OUTGOING
    )
    private ThanhPhan thanhphan;

    public int getBatDau() {
        return batDau;
    }

    public void setBatDau(int batDau) {
        this.batDau = batDau;
    }

    public int getKetThuc() {
        return ketThuc;
    }

    public void setKetThuc(int ketThuc) {
        this.ketThuc = ketThuc;
    }

    public List<Float> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
    
    public NER getNer() {
        return ner;
    }

    public void setNer(NER ner) {
        this.ner = ner;
    }

    public ThanhPhan getThanhphan() {
        return thanhphan;
    }

    public void setThanhphan(ThanhPhan thanhphan) {
        this.thanhphan = thanhphan;
    }
   
    
}
