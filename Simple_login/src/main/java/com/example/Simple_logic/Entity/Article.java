package com.example.Simple_logic.Entity;

import java.util.HashSet;
import java.util.UUID;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name = "Article")
@JsonFilter ("Filter")
public class Article {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column (name ="title")
    private String title ; 

    @Column (name = "subject")
    private String subject ;

    @Column (name = "content",columnDefinition = "TEXT")
    private String words ; 

    @Column (name = "version")
    private String version ;

    @ManyToOne
    @JoinColumn (name = "id_group")
    private GroupVersion groupVersion;

    @Column (name = "status")
    private String status ;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    @JsonIgnore 
    private float[] embedding;

    @ManyToMany 
    @JoinTable (
        name = "article_reported_customer",
        joinColumns = @JoinColumn(name = "idarticle"),
        inverseJoinColumns = @JoinColumn  (name = "idcustomer")
    )
    private Set<Customer> customersReport = new HashSet<>();
    public Set<Customer> getCustomersReport(){return customersReport;}
    public void setCustomersReport(Set<Customer> customersReport){this.customersReport = customersReport; }

    // Default Constuctor 
    public Article() {
    }

    // Constructor
    public Article(String title, String subject, String words, String version, GroupVersion groupVersion, String status) {
        this.title = title;
        this.subject = subject;
        this.words = words;
        this.version = version;
        this.groupVersion = groupVersion;
        this.status = status;
    }

    // Getters && Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getWords() { return words; }
    public void setWords(String words) { this.words = words; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public GroupVersion getId_group() { return groupVersion; }
    public void setId_group(GroupVersion groupVersion) { this.groupVersion = groupVersion ; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
