package com.example.Simple_logic.Entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Node("Thành phần")
public abstract class ThanhPhan {
    @Id
    private String id;

    @Property ("Nội dung")
    private String NoiDung ;

    @Relationship ("Thành phần - Văn bản")
    private List<ThanhPhan_VanBan> thanhPhan_VanBans = new ArrayList<>() ;

    @Relationship ("Thành phần - Thành phần")
    private List<ThanhPhan_ThanhPhan> thanhPhan_ThanhPhans = new ArrayList<>() ;

    @Property ("Loại thành phần")
    private String loaithanhphan;

    @Property("Embedding")
    private List<Double> embedding;

    @Relationship (
        type = "Nhắc đến NER",
        direction = Relationship.Direction.OUTGOING
    )
    private List<NER> ners = new ArrayList<>();

    @Property ("EmbeddingGoc")
    private List<Double> embeddingGoc;

    @Relationship (
        type = "Thành phần thuộc Văn bản",
        direction = Relationship.Direction.OUTGOING
    )   
    private VanBan vanBan;
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

    public String getNoiDung() {
        return NoiDung;
    }

    public void setNoiDung(String noiDung) {
        NoiDung = noiDung;
    }

    public VanBan getVanBan() {
        return vanBan;
    }

    public void setVanBan(VanBan vanBan) {
        this.vanBan = vanBan;
    }

    public String getLoaithanhphan() {
        return loaithanhphan;
    }

    public void setLoaithanhphan(String loaithanhphan) {
        this.loaithanhphan = loaithanhphan;
    }

    @JsonIgnore
    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    public List<NER> getNers() {
        return ners;
    }

    public void setNers(List<NER> ners) {
        this.ners = ners;
    }

    @JsonIgnore
    public List<Double> getEmbeddingGoc() {
        return embeddingGoc;
    }
    
    public void setEmbeddingGoc(List<Double> embeddingGoc) {
        this.embeddingGoc = embeddingGoc;
    }

    
    
}