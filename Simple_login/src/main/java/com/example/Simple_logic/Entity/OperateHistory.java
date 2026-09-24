package com.example.Simple_logic.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "operate_history")
public class OperateHistory {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne 
    @JoinColumn(name = "id_article")
    private Article article;

    @Column(name = "release_version", length = 45)
    private String version;

    @ManyToOne 
    @JoinColumn(name = "admin_iduser")
    private Admin admin;

    @Column(name = "type", length = 45)
    private String type;

    @Column(name = "time_stamp")
    private LocalDateTime created_at;

    // Default Constructor
    public OperateHistory() {
    }

    // Constructor
    public OperateHistory(Article article, String version, Admin admin, String type, LocalDateTime created_at) {
        this.article = article;
        this.version = version;
        this.admin = admin;
        this.type = type;
        this.created_at = created_at;
    }

    // Getters && Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}